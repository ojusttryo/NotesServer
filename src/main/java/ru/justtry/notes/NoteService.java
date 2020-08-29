package ru.justtry.notes;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.or;

import java.util.List;
import java.util.regex.Pattern;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.justtry.database.Database;
import ru.justtry.mappers.NoteMapper;
import ru.justtry.metainfo.Attribute;
import ru.justtry.shared.Identifiable;
import ru.justtry.shared.NoteConstants;
import ru.justtry.shared.Utils;

@Component
public class NoteService
{
    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private Database database;
    @Autowired
    private Utils utils;


    public Note get(String collection, String id)
    {
        Document doc = database.getDocument(collection, id);
        Note note = (Note)noteMapper.getObject(doc);
        return note;
    }


    public void save(String collection, Note note)
    {
        Document document = noteMapper.getDocument(note);
        String id = database.saveDocument(collection, document);
        note.setId(id);
    }


    public void update(String collection, Note note)
    {
        Document doc = noteMapper.getDocument(note);
        database.updateDocument(collection, doc);
    }


    /**
     * Saves attributes which were used before. They probably will be useful in future
     * when attributes will have been changed again.
     */
    public void copyUnusedAttributes(Note newNote, Note oldNote)
    {
        for (String key : oldNote.getAttributes().keySet())
        {
            if (!newNote.getAttributes().containsKey(key))
                newNote.getAttributes().put(key, oldNote.getAttributes().get(key));
        }
    }


    public Identifiable[] searchBySubstring(String substring, String collection, Attribute attr)
    {
        String substringPattern = ".*" + Pattern.quote(substring) + ".*";
        Pattern pattern = Pattern.compile(substringPattern, Pattern.CASE_INSENSITIVE);
        String field = getDbFieldName(attr.getName());
        Document search = new Document();
        search.put(field, pattern);

        List<Document> docs = database.getDocuments(collection, search);
        pattern.matcher(substring).reset();
        // If some notes haven't this attribute, but default value is similar to requested string,
        // response should contains all notes without specified attribute.
        if (attr.getDefaultValue() != null && pattern.matcher(attr.getDefaultValue()).find())
            docs.addAll(database.getDocuments(collection, exists(field, false)));

        return docs == null ? null : noteMapper.getObjects(docs);
    }


    public Identifiable[] searchByNumber(String number, String collection, Attribute attr)
    {
        Double doubleValue = Double.parseDouble(number);
        Double defaultNumber = attr.getDefaultValue() == null ? null
                : Double.parseDouble(attr.getDefaultValue());
        Double step = attr.getStep() == null ? null : Double.parseDouble(attr.getStep());
        String field = getDbFieldName(attr.getName());
        List<Document> docs;
        if (defaultNumber != null && utils.equals(doubleValue, defaultNumber, step != null ? step : 0.00001))
            docs = database.getDocuments(collection, or(eq(field, doubleValue), exists(field, false)));
        else
            docs = database.getDocuments(collection, eq(field, doubleValue));

        return docs == null ? null : noteMapper.getObjects(docs);
    }


    public Identifiable[] searchByExactString(String string, String collection, Attribute attr)
    {
        String field = getDbFieldName(attr.getName());
        List<Document> documents;
        if (attr.getDefaultValue() != null && attr.getDefaultValue().contentEquals(string))
            documents = database.getDocuments(collection, or(eq(field, string), exists(field, false)));
        else
            documents = database.getDocuments(collection, eq(field, string));

        return documents == null ? null : noteMapper.getObjects(documents);
    }


    public Identifiable[] searchByBoolean(String value, String collection, Attribute attr)
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
            documents = database.getDocuments(collection, or(eq(field, booleanValue), exists(field, false)));
        else
            documents = database.getDocuments(collection, eq(field, booleanValue));

        return documents == null ? null : noteMapper.getObjects(documents);
    }


    public Identifiable[] searchByIngoing(String value, String collection, Attribute attr)
    {
        List<Document> documents = database.getDocuments(collection, in(getDbFieldName(attr.getName()), value));
        return documents == null ? null : noteMapper.getObjects(documents);
    }


    private String getDbFieldName(String attributeName)
    {
        return String.format("%s.%s", NoteConstants.ATTRIBUTES, attributeName);
    }
}
