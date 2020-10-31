package ru.justtry.database.migrations;

import java.util.List;
import java.util.Map.Entry;

import org.bson.Document;
import org.springframework.context.ApplicationContext;

import com.github.ojusttryo.migmong.changeset.ChangeLog;
import com.github.ojusttryo.migmong.changeset.ChangeSet;
import com.mongodb.BasicDBObject;

import ru.justtry.database.Database;
import ru.justtry.metainfo.Entity;
import ru.justtry.metainfo.EntityService;
import ru.justtry.rest.NotesController;
import ru.justtry.shared.NoteConstants;

@ChangeLog
public class V_0_1_0__changeAttributesArrayToObject
{
    private final String FAVOURITE = "favourite";

    @ChangeSet(id = 1)
    public void changeAttributesArrayToObject(ApplicationContext context)
    {
        Database db = context.getBean(Database.class);
        EntityService entityService = context.getBean(EntityService.class);
        NotesController notesController = context.getBean(NotesController.class);

        Entity[] entities = entityService.getAll();
        for (Entity entity : entities)
        {
            String collectionName = notesController.getCollectionName(entity.getName());
            List<Document> notes = db.getDocuments(collectionName, entity.getKeyAttribute());

            for (Document note : notes)
            {
                // The old field used for testing
                if (note.containsKey(NoteConstants.ATTRIBUTES + "2"))
                {
                    note.remove(NoteConstants.ATTRIBUTES + "2");
                    db.unsetAttribute(collectionName, note, NoteConstants.ATTRIBUTES + "2");
                    db.updateDocument(collectionName, note);
                }

                // Start changing only if attributes field is still list (ArrayList)
                Object attributesField = note.get(NoteConstants.ATTRIBUTES);
                if (attributesField instanceof List)
                {
                    List<Document> attributes = (List<Document>)attributesField;
                    BasicDBObject newAttributes = new BasicDBObject();
                    for (Document attribute : attributes)
                    {
                        Entry<String, Object> entry = attribute.entrySet().iterator().next();
                        newAttributes.put(entry.getKey(), entry.getValue());
                    }

                    note.put(NoteConstants.ATTRIBUTES, newAttributes);

                    db.updateDocument(collectionName, note);
                }
            }
        }
    }


    /**
     * A migration for my private usage to fix current collection state after changing attribute.
     * There are no change attribute mechanism yet, so it will have been done by migration
     */
    @ChangeSet(id = 2)
    public void fixDiaryDateAttributeName(ApplicationContext context)
    {
        Database db = context.getBean(Database.class);
        EntityService entityService = context.getBean(EntityService.class);
        NotesController notesController = context.getBean(NotesController.class);

        Entity diary = entityService.getByName("diary");
        String collectionName = notesController.getCollectionName(diary.getName());

        List<Document> notes = db.getDocuments(collectionName, diary.getKeyAttribute());
        for (Document note : notes)
        {
            // We can use this migration only if the previous one has changed attributes field
            Object attributesField = note.get(NoteConstants.ATTRIBUTES);
            if (!(attributesField instanceof Document))
                continue;

            Document attributes = (Document)note.get(NoteConstants.ATTRIBUTES);

            // Nothing to do
            if (!attributes.containsKey("date"))
                continue;

            Object date = attributes.get("date");
            Object dateRequired = attributes.get("date-required");

            if (dateRequired == null && date != null)
                attributes.put("date-required", date);
            attributes.remove("date");

            note.put(NoteConstants.ATTRIBUTES, attributes);

            db.updateDocument(collectionName, note);
        }
    }


    /**
     * Rename note field 'favourite' to 'favorite'
     */
    @ChangeSet(id = 3)
    public void renameFavourite(ApplicationContext context)
    {
        Database db = context.getBean(Database.class);
        EntityService entityService = context.getBean(EntityService.class);
        NotesController notesController = context.getBean(NotesController.class);

        Entity[] entities = entityService.getAll();
        for (Entity entity : entities)
        {
            String collectionName = notesController.getCollectionName(entity.getName());
            List<Document> notes = db.getDocuments(collectionName, entity.getKeyAttribute());

            for (Document note : notes)
            {
                if (note.containsKey(FAVOURITE))
                {
                    if (note.get(FAVOURITE) != null)
                        note.put(NoteConstants.FAVORITE, note.get(FAVOURITE));

                    note.remove(FAVOURITE);
                    db.unsetAttribute(collectionName, note, FAVOURITE);
                    db.updateDocument(collectionName, note);
                }
            }
        }
    }
}

