package ru.justtry.mappers;

import static ru.justtry.shared.Constants.MONGO_ID;
import static ru.justtry.shared.NoteConstants.FAVORITE;
import static ru.justtry.shared.NoteConstants.HIDDEN;
import static ru.justtry.shared.NoteConstants.NESTED;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.mongodb.BasicDBObject;

import ru.justtry.notes.Note;
import ru.justtry.shared.Identifiable;
import ru.justtry.shared.NoteConstants;

/**
 * Mapper to use note document, which is saved like this:
 *
 * {
 *     "id": "5d8ad8ad43e02d58f3a59945",
 *     hidden: false,
 *     "attributes":
 *     {
 *         "name": "Avengers 5",
 *         "state": "Finished",
 *         "year": 2025
 *     }
 * }
 */
@Component
public class NoteMapper extends Mapper
{
    @Override
    public Identifiable getObject(Document document)
    {
        Note note = new Note();
        note.setId(document.get(MONGO_ID).toString());
        note.setHidden((boolean)document.get(HIDDEN));
        note.setFavorite((boolean)document.get(FAVORITE));
        note.setNested(getStringOrNull(document, NESTED));
        Document attributes = (Document)document.get(NoteConstants.ATTRIBUTES);
        for (String key : attributes.keySet())
        {
            note.getAttributes().put(key, attributes.get(key));
        }

        return note;
    }


    @Override
    public Document getDocument(Identifiable object)
    {
        Note note = (Note)object;

        Document document = new Document();
        document.append(HIDDEN, note.isHidden());
        document.append(FAVORITE, note.isFavorite());
        if (!Strings.isNullOrEmpty(note.getNested()))
            document.append(NESTED, note.getNested());
        if (!Strings.isNullOrEmpty(note.getId()))
            document.append(MONGO_ID, new ObjectId(note.getId()));

        BasicDBObject attributes = new BasicDBObject();
        for (String attrName : note.getAttributes().keySet())
            attributes.put(attrName, note.getAttributes().get(attrName));
        document.append(NoteConstants.ATTRIBUTES, attributes);

        return document;
    }
}
