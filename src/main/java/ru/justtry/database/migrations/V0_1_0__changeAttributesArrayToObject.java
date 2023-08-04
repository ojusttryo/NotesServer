package ru.justtry.database.migrations;

import static ru.justtry.shared.Constants.MONGO_ID;

import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.slf4j.MDC;

import com.github.ojusttryo.migmong.migration.MigrationContext;
import com.github.ojusttryo.migmong.migration.annotations.Migration;
import com.github.ojusttryo.migmong.migration.annotations.MigrationUnit;
import com.mongodb.BasicDBObject;

import ru.justtry.database.Database;
import ru.justtry.metainfo.Entity;
import ru.justtry.metainfo.EntityService;
import ru.justtry.rest.NotesController;
import ru.justtry.shared.NoteConstants;

@Migration
public class V0_1_0__changeAttributesArrayToObject
{
    final static Logger logger = LogManager.getLogger(V0_1_0__changeAttributesArrayToObject.class);

    @MigrationUnit(id = 1)
    public void changeAttributesArrayToObject(MigrationContext context)
    {
        MDC.put("migration", this.getClass().getSimpleName());

        Database db = context.getApplicationContext().getBean(Database.class);
        EntityService entityService = context.getApplicationContext().getBean(EntityService.class);
        NotesController notesController = context.getApplicationContext().getBean(NotesController.class);

        Entity[] entities = entityService.getAll();
        for (Entity entity : entities)
        {
            MDC.put("entity", entity.getName());

            String collectionName = notesController.getCollectionName(entity.getName());
            List<Document> notes = db.getDocuments(collectionName, entity.getKeyAttribute());

            for (Document note : notes)
            {
                MDC.put("note", note.get(MONGO_ID).toString());

                // The old field used for testing
                if (note.containsKey(NoteConstants.ATTRIBUTES + "2"))
                {
                    note.remove(NoteConstants.ATTRIBUTES + "2");
                    db.unsetAttribute(collectionName, note, NoteConstants.ATTRIBUTES + "2");
                    db.updateDocument(collectionName, note);
                    logger.info(String.format("The old field %s has been removed", NoteConstants.ATTRIBUTES + "2"));
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

                    logger.info(String.format("The attribute field has been changed from array to object"));
                }
            }
        }

        MDC.remove("migration");
        MDC.remove("entity");
        MDC.remove("note");
    }


    /**
     * A migration for my private usage to fix current collection state after changing attribute.
     * There are no change attribute mechanism yet, so it will have been done by migration
     */
    @MigrationUnit(id = 2)
    public void fixDiaryDateAttributeName(MigrationContext context)
    {
        MDC.put("migration", this.getClass().getSimpleName());

        Database db = context.getApplicationContext().getBean(Database.class);
        EntityService entityService = context.getApplicationContext().getBean(EntityService.class);
        NotesController notesController = context.getApplicationContext().getBean(NotesController.class);

        Entity diary = entityService.getByName("diary");
        String collectionName = notesController.getCollectionName(diary.getName());

        MDC.put("entity", diary.getName());

        List<Document> notes = db.getDocuments(collectionName, diary.getKeyAttribute());
        for (Document note : notes)
        {
            MDC.put("note", note.get(MONGO_ID).toString());

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

            logger.info("Date attribute has been changed from 'date' to 'date-required'");
        }

        MDC.remove("migration");
        MDC.remove("entity");
        MDC.remove("note");
    }


    /**
     * Rename note field 'favourite' to 'favorite'
     */
    @MigrationUnit(id = 3)
    public void renameFavourite(MigrationContext context)
    {
        MDC.put("migration", this.getClass().getSimpleName());

        Database db = context.getApplicationContext().getBean(Database.class);
        EntityService entityService = context.getApplicationContext().getBean(EntityService.class);
        NotesController notesController = context.getApplicationContext().getBean(NotesController.class);
        final String FAVOURITE = "favourite";

        Entity[] entities = entityService.getAll();
        for (Entity entity : entities)
        {
            MDC.put("entity", entity.getName());

            String collectionName = notesController.getCollectionName(entity.getName());
            List<Document> notes = db.getDocuments(collectionName, entity.getKeyAttribute());

            for (Document note : notes)
            {
                MDC.put("note", note.get(MONGO_ID).toString());

                if (note.containsKey(FAVOURITE))
                {
                    if (note.get(FAVOURITE) != null)
                        note.put(NoteConstants.FAVORITE, note.get(FAVOURITE));

                    note.remove(FAVOURITE);
                    db.unsetAttribute(collectionName, note, FAVOURITE);
                    db.updateDocument(collectionName, note);

                    logger.info("The 'favourite' attribute has been renamed to 'favorite'");
                }
            }
        }

        MDC.remove("migration");
        MDC.remove("entity");
        MDC.remove("note");
    }
}

