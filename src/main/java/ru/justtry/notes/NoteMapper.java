package ru.justtry.notes;

import com.google.common.base.Strings;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
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
        List<Document> attributes = (List<Document>)document.get(NoteConstants.ATTRIBUTES);
        for (Document attribute : attributes)
        {
            for (Map.Entry<String, Object> entry : attribute.entrySet())
            {
                note.getAttributes().add(entry);
                break;  // expected only one
            }
//            Object property = attribute.get(0);
//            Object entry = (Map.Entry<String, Object>)property;
//            note.getAttributes().add((Map.Entry<String, Object>)entry);
        }
        //note.setAttributes((List<Map.Entry<String, Object>>)document.get(NoteConstants.ATTRIBUTES));

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
        /**
         * For now the document saved like this:
         * {
         *     "id": "5d8ad8ad43e02d58f3a59945",
         *     "folderId": null,
         *     "attributes": [
         *       { "5d8a5a87602f051474ea6a4e": "Avengers 5" },
         *       { "5d8a5b17602f051474ea6a50": "Finished" },
         *       { "5d8a6035afa6f80313f050c3": 2025 }
         *     ]
         * }
         * In the future attributes could be redone to a single object with properties or it could be just
         * properties of the note object.
         */

        Note note = (Note)object;

        Document document = new Document();
        document.append(FOLDER_ID, note.getFolderId());
        if (!Strings.isNullOrEmpty(note.getId()))
            document.append(MONGO_ID, new ObjectId(note.getId()));

//        List<Document> attributes = new ArrayList<>();
        List<DBObject> attributes = new ArrayList<>();
        for (Map.Entry<String, Object> attribute : note.getAttributes())
        {
            BasicDBObject attr = new BasicDBObject();
            attr.put(attribute.getKey(), attribute.getValue());
            attributes.add(attr);


//            attributes.add(new Document().append(attribute.getKey(), attribute.getValue()));

//            attributes.append(attribute.getKey(), attribute.getValue());
//            Document attrDocument = new Document()
//                    .append(NAME, attribute.getName())
//                    .append(VALUE, attribute.getValue());
//            attributes.add(attrDocument);
        }
        document.append(NoteConstants.ATTRIBUTES, attributes);

        return document;
    }
}
