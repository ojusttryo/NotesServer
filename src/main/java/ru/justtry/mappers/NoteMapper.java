package ru.justtry.mappers;

import static ru.justtry.shared.Constants.MONGO_ID;
import static ru.justtry.shared.NoteConstants.FOLDER_ID;
import static ru.justtry.shared.NoteConstants.HIDDEN;
import static ru.justtry.shared.NoteConstants.NESTED;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import ru.justtry.notes.Note;
import ru.justtry.shared.Identifiable;
import ru.justtry.shared.NoteConstants;

@Component
public class NoteMapper extends Mapper
{
    @Override
    public Identifiable getObject(Document document)
    {
        Note note = new Note();
        note.setId(document.get(MONGO_ID).toString());
        note.setFolderId(getStringOrNull(document, FOLDER_ID));
        note.setHidden((boolean)document.get(HIDDEN));
        note.setNested(getStringOrNull(document, NESTED));
        List<Document> attributes = (List<Document>)document.get(NoteConstants.ATTRIBUTES);
        for (Document attribute : attributes)
        {
            Map.Entry<String, Object> entry = attribute.entrySet().iterator().next();
            note.getAttributes().put(entry.getKey(), entry.getValue());
        }

        return note;
    }


    @Override
    public Document getDocument(Identifiable object)
    {
        /**
         * The document saved like this:
         * {
         *     "id": "5d8ad8ad43e02d58f3a59945",
         *     "folderId": null,
         *     "attributes": [
         *       { "name": "Avengers 5" },
         *       { "state": "Finished" },
         *       { "year": 2025 }
         *     ]
         * }
         */

        Note note = (Note)object;

        Document document = new Document();
        document.append(FOLDER_ID, note.getFolderId());
        document.append(HIDDEN, note.isHidden());
        if (!Strings.isNullOrEmpty(note.getNested()))
            document.append(NESTED, note.getNested());
        if (!Strings.isNullOrEmpty(note.getId()))
            document.append(MONGO_ID, new ObjectId(note.getId()));

        List<DBObject> attributes = new ArrayList<>();
        for (String attrName : note.getAttributes().keySet())
        {
            BasicDBObject attr = new BasicDBObject();
            attr.put(attrName, note.getAttributes().get(attrName));
            attributes.add(attr);
        }
        document.append(NoteConstants.ATTRIBUTES, attributes);

        return document;
    }
}
