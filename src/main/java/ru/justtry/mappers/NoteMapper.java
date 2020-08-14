package ru.justtry.mappers;

import static ru.justtry.shared.Constants.MONGO_ID;
import static ru.justtry.shared.NoteConstants.FOLDER_ID;

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
         * For now the document saved like this:
         * {
         *     "id": "5d8ad8ad43e02d58f3a59945",
         *     "folderId": null,
         *     "metainfo": [
         *       { "5d8a5a87602f051474ea6a4e": "Avengers 5" },
         *       { "5d8a5b17602f051474ea6a50": "Finished" },
         *       { "5d8a6035afa6f80313f050c3": 2025 }
         *     ]
         * }
         * In the future metainfo could be redone to a single object with properties or it could be just
         * properties of the note object.
         */

        Note note = (Note)object;

        Document document = new Document();
        document.append(FOLDER_ID, note.getFolderId());
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
