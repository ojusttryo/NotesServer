package ru.justtry.notes;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.or;
import static ru.justtry.shared.Constants.MONGO_ID;
import static ru.justtry.shared.NoteConstants.HIDDEN;
import static ru.justtry.shared.NoteConstants.NESTED;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.justtry.database.Database;
import ru.justtry.database.sort.SortInfo;
import ru.justtry.mappers.NoteMapper;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.dictionary.Type;
import ru.justtry.metainfo.AttributeService;
import ru.justtry.metainfo.Entity;
import ru.justtry.metainfo.EntityService;
import ru.justtry.shared.Identifiable;
import ru.justtry.shared.NoteConstants;
import ru.justtry.shared.Utils;

@Service
@RequiredArgsConstructor
public class NoteService
{
    private static final Bson NOT_HIDDEN_FILTER = eq(HIDDEN, false);
    private static final Bson HIDDEN_FILTER = eq(HIDDEN, true);
    private static final Bson NOT_NESTED_FILTER = exists(NESTED, false);
    private static final Bson REGULAR_FILTER = and(NOT_HIDDEN_FILTER, NOT_NESTED_FILTER);

    private final NoteMapper noteMapper;
    private final Database database;
    private final Utils utils;
    private final AttributeService attributeService;
    private final EntityService entityService;


    public Note get(String collection, String id)
    {
        Document doc = database.getDocument(collection, id);
        Note note = (Note)noteMapper.getObject(doc);
        return note;
    }


    public Object getKey(String entity, String id)
    {
        Entity e = entityService.getByName(entity);
        Document doc = database.getDocument(getCollectionName(entity), id);
        Document attributes = (Document)doc.get(NoteConstants.ATTRIBUTES);
        return attributes.get(e.getKeyAttribute());
    }


    public Note[] get(String entity)
    {
        Entity e = entityService.getByName(entity);
        if (e == null)
            throw new IllegalArgumentException("Wrong entity name: " + entity);

        String sortField = String.format("%s.%s", NoteConstants.ATTRIBUTES, createSortInfo(e).getAttribute().getName());
        List<Document> documents = database.getDocuments(getCollectionName(entity), sortField);
        Identifiable[] objects = noteMapper.getObjects(documents);

        return toNoteArray(objects);
    }


    /**
     * Get by regular filter: not nested and not hidden
     * @param entity entity name
     * @return array of found notes
     */
    public Note[] getRegular(String entity)
    {
        Entity e = entityService.getByName(entity);
        if (e == null)
            throw new IllegalArgumentException("Wrong entity name: " + entity);

        List<Document> documents = database.getDocuments(getCollectionName(entity), REGULAR_FILTER,
                createSortInfo(e));
        Identifiable[] objects = noteMapper.getObjects(documents);

        return toNoteArray(objects);
    }


    public Note[] getNested(String entity, String nestedValue)
    {
        Entity e = entityService.getByName(entity);
        if (e == null)
            throw new IllegalArgumentException("Wrong entity name: " + entity);
        List<Document> documents = database.getDocuments(getCollectionName(entity),
                eq(NESTED, nestedValue), createSortInfo(e));
        Identifiable[] objects = noteMapper.getObjects(documents);
        return toNoteArray(objects);
    }


    public Note[] get(String entity, List<String> ids)
    {
        List<Document> documents = database.getDocuments(getCollectionName(entity), ids, MONGO_ID);
        Identifiable[] notes = noteMapper.getObjects(documents);
        return toNoteArray(notes);
    }


    public void save(String entity, Note note)
    {
        saveTimeAttributes(attributeService.get(entity), note);
        Document document = noteMapper.getDocument(note);
        String id = database.saveDocument(getCollectionName(entity), document);
        note.setId(id);
    }


    public void update(String entity, Note note)
    {
        updateTimeAttributes(attributeService.get(entity), note);
        Document doc = noteMapper.getDocument(note);
        database.updateDocument(getCollectionName(entity), doc);
    }


    public void updateTimeAttributes(Attribute[] attributes, Note note)
    {
        long now = utils.getTimeInMs();
        for (Attribute attribute : attributes)
            updateTimeAttribute(attribute, note, now);
    }


    public void saveTimeAttributes(Attribute[] attributes, Note note)
    {
        long now = utils.getTimeInMs();
        for (Attribute attribute : attributes)
        {
            if (Type.isTimestampType(attribute.getType()))
                note.getAttributes().put(attribute.getName(), now);
        }
    }


    private void updateTimeAttribute(Attribute attribute, Note note, long time)
    {
        boolean isUpdateTime = (attribute.getTypeAsEnum() == Type.UPDATE_TIME);
        boolean isSaveTime = (attribute.getTypeAsEnum() == Type.SAVE_TIME);
        boolean saveTimeIsAbsent = isSaveTime && (note.getAttributes().get(attribute.getName()) == null);
        if (isUpdateTime || saveTimeIsAbsent)
            note.getAttributes().put(attribute.getName(), time);
    }


    public void hide(String entity, String id)
    {
        Note note = get(getCollectionName(entity), id);
        note.setHidden(true);
        update(entity, note);
    }


    public void reveal(String entity, String id)
    {
        Note note = get(getCollectionName(entity), id);
        note.setHidden(false);
        update(entity, note);
    }


    /**
     * Saves attributes which were used before. They probably will be useful in future
     * when attributes will have been changed again.
     */
    public void copyUnusedAttributes(Map<String, Attribute> attributes, Note newNote, Note oldNote)
    {
        for (String key : oldNote.getAttributes().keySet())
        {
            if (!attributes.containsKey(key) && !newNote.getAttributes().containsKey(key))
                newNote.getAttributes().put(key, oldNote.getAttributes().get(key));
        }
    }


    public void copyTimeAttributes(Map<String, Attribute> attributes, Note newNote, Note oldNote)
    {
        for (String key : oldNote.getAttributes().keySet())
        {
            Attribute attribute = attributes.get(key);
            if (attribute == null || !Type.isTimestampType(attribute.getType()))
                continue;

            if (oldNote.getAttributes().containsKey(key) && !newNote.getAttributes().containsKey(key))
                newNote.getAttributes().put(key, oldNote.getAttributes().get(key));
        }
    }


    public Identifiable[] searchBySubstring(String substring, String collection, Attribute attr, SortInfo sortInfo)
    {
        String substringPattern = ".*" + Pattern.quote(substring) + ".*";
        Pattern pattern = Pattern.compile(substringPattern, Pattern.CASE_INSENSITIVE);
        String field = getDbFieldName(attr.getName());
        Document search = new Document();
        search.put(field, pattern);

        List<Document> docs;
        // If some notes haven't this attribute, but default value is similar to requested string,
        // response should contains all notes without specified attribute.
        if (attr.getDefaultValue() != null && pattern.matcher(attr.getDefaultValue()).find())
        {
            pattern.matcher(substring).reset();
            Bson filter = and(or(exists(field, false), search), REGULAR_FILTER);
            docs = database.getDocuments(collection, filter, sortInfo);
        }
        else
        {
            docs = database.getDocuments(collection, and(search, REGULAR_FILTER), sortInfo);
        }

        return docs == null ? null : noteMapper.getObjects(docs);
    }


    public Identifiable[] searchByNumber(String number, String collection, Attribute attr, SortInfo sortInfo)
    {
        Double doubleValue = Double.parseDouble(number);
        Double defaultNumber = attr.getDefaultValue() == null ? null
                : Double.parseDouble(attr.getDefaultValue());
        Double step = attr.getStep() == null ? null : Double.parseDouble(attr.getStep());
        String field = getDbFieldName(attr.getName());
        List<Document> docs;
        if (defaultNumber != null && utils.equals(doubleValue, defaultNumber, step != null ? step : 0.00001))
        {
            docs = database.getDocuments(collection,
                    and(or(eq(field, doubleValue), exists(field, false)), REGULAR_FILTER), sortInfo);
        }
        else
        {
            docs = database.getDocuments(collection, and(eq(field, doubleValue), REGULAR_FILTER), sortInfo);
        }

        return docs == null ? null : noteMapper.getObjects(docs);
    }


    public Identifiable[] searchByExactString(String string, String collection, Attribute attr, SortInfo sortInfo)
    {
        String field = getDbFieldName(attr.getName());
        List<Document> documents;
        if (attr.getDefaultValue() != null && attr.getDefaultValue().contentEquals(string))
        {
            documents = database.getDocuments(collection,
                    and(or(eq(field, string), exists(field, false)), REGULAR_FILTER), sortInfo);
        }
        else
        {
            documents = database.getDocuments(collection, and(eq(field, string), REGULAR_FILTER), sortInfo);
        }

        return documents == null ? null : noteMapper.getObjects(documents);
    }


    public Identifiable[] searchByBoolean(String value, String collection, Attribute attr, SortInfo sortInfo)
    {
        String field = getDbFieldName(attr.getName());
        boolean booleanValue = Boolean.parseBoolean(value);
        Boolean defaultBoolean = attr.getDefaultValue() == null ? null
                : Boolean.parseBoolean(attr.getDefaultValue());
        boolean isSet = defaultBoolean != null;
        boolean isTrue = defaultBoolean != null && defaultBoolean == true;
        boolean isFalse = defaultBoolean != null && defaultBoolean == false;

        List<Document> documents;
        if ((!booleanValue && (!isSet || isFalse)) || (booleanValue && isTrue))
        {
            documents = database.getDocuments(collection,
                    and(or(eq(field, booleanValue), exists(field, false)), REGULAR_FILTER), sortInfo);
        }
        else
        {
            documents = database.getDocuments(collection, and(eq(field, booleanValue), REGULAR_FILTER), sortInfo);
        }

        return documents == null ? null : noteMapper.getObjects(documents);
    }


    public Identifiable[] searchByIngoing(String value, String collection, Attribute attr, SortInfo sortInfo)
    {
        List<Document> documents = database.getDocuments(collection,
                and(in(getDbFieldName(attr.getName()), value), REGULAR_FILTER), sortInfo);
        return documents == null ? null : noteMapper.getObjects(documents);
    }


    public Identifiable[] searchByHidden(String collection, Boolean hidden, SortInfo sortInfo)
    {
        List<Document> documents = database.getDocuments(collection,
                hidden ? and(HIDDEN_FILTER, NOT_NESTED_FILTER) : and(NOT_HIDDEN_FILTER, NOT_NESTED_FILTER),
                sortInfo);
        return documents == null ? null : noteMapper.getObjects(documents);
    }


    private String getDbFieldName(String attributeName)
    {
        return String.format("%s.%s", NoteConstants.ATTRIBUTES, attributeName);
    }


    public SortInfo createSortInfo(Entity entity)
    {
        if (entity.getSortAttribute() == null)
            return null;

        Attribute attribute = attributeService.getByName(entity.getSortAttribute());
        SortInfo sortInfo = new SortInfo(attribute, entity.getSortDirection());
        return sortInfo;
    }


    public String getCollectionName(String entity)
    {
        return entity + ".notes";
    }


    private Note[] toNoteArray(Identifiable[] notes)
    {
        return Arrays.stream(notes).map(x -> (Note)x).toArray(Note[]::new);
    }
}
