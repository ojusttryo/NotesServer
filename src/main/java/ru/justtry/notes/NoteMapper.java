package ru.justtry.notes;

import com.google.common.base.Strings;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import ru.justtry.attributes.Entity;
import ru.justtry.notes.Note;
import ru.justtry.notes.NoteAttribute;
import ru.justtry.notes.NoteConstants;
import ru.justtry.shared.Mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.justtry.attributes.EntityConstants.ATTRIBUTES;
import static ru.justtry.notes.NoteConstants.*;
import static ru.justtry.shared.Constants.MONGO_ID;
import static ru.justtry.shared.Constants.NAME;

@Component
public class NoteMapper extends Mapper
{
    @Override
    public Object getObject(Document document)
    {
        Note note = new Note();
        note.setId(document.get(MONGO_ID).toString());
        note.setFolderId(getStringOrNull(document, FOLDER_ID));
        note.setAttributes((List<NoteAttribute>)document.get(NoteConstants.ATTRIBUTES));

        return note;
    }

    @Override
    public Document getDocument(String id, Map<String, Object> values)
    {
        Document document = new Document();

        if (!Strings.isNullOrEmpty(id))
            document.append(MONGO_ID, new ObjectId(id));

        document.append(FOLDER_ID, values.get(FOLDER_ID));
        document.append(ATTRIBUTES, values.get(ATTRIBUTES));

        return document;
    }

    @Override
    public Document getDocument(Object object)
    {
        Note note = (Note)object;

        Document document = new Document();
        document.append(FOLDER_ID, note.getFolderId());
        if (!Strings.isNullOrEmpty(note.getId()))
            document.append(MONGO_ID, new ObjectId(note.getId()));

        List<Document> attributes = new ArrayList<>();
        for (NoteAttribute attribute : note.getAttributes())
        {
            Document attrDocument = new Document()
                    .append(NAME, attribute.getName())
                    .append(VALUE, attribute.getValue());
            attributes.add(attrDocument);
        }
        document.append(NoteConstants.ATTRIBUTES, attributes);

        return document;
    }
}
