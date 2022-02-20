package ru.justtry.database.migrations;

import static ru.justtry.shared.Constants.APPLICATION_CONTEXT;
import static ru.justtry.shared.Constants.MONGO_ID;

import java.util.List;
import java.util.Map.Entry;

import org.bson.Document;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;

import com.github.migmong.migration.MigrationContext;
import com.github.migmong.migration.annotations.Migration;
import com.github.migmong.migration.annotations.MigrationUnit;
import com.mongodb.BasicDBObject;

import lombok.extern.slf4j.Slf4j;
import ru.justtry.database.Database;
import ru.justtry.metainfo.Entity;
import ru.justtry.metainfo.EntityService;
import ru.justtry.notes.NoteService;
import ru.justtry.shared.NoteConstants;

@Migration
@Slf4j
public class V0_1_0__changeAttributesArrayToObject
{
    @MigrationUnit(id = 1)
    public void changeAttributesArrayToObject(MigrationContext context)
    {
        MDC.put("migration", this.getClass().getSimpleName());

        ApplicationContext applicationContext = (ApplicationContext)context.getVariable(APPLICATION_CONTEXT);
        Database db = applicationContext.getBean(Database.class);
        EntityService entityService = applicationContext.getBean(EntityService.class);
        NoteService noteService = applicationContext.getBean(NoteService.class);

        Entity[] entities = entityService.getAll();
        for (Entity entity : entities)
        {
            MDC.put("entity", entity.getName());

            String collectionName = noteService.getCollectionName(entity.getName());
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
                    log.info(String.format("The old field %s has been removed", NoteConstants.ATTRIBUTES + "2"));
                }

                // Start changing only if attributes field is still the list (ArrayList)
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

                    log.info("The attribute field has been changed from array to object");
                }
            }
        }

        MDC.remove("migration");
        MDC.remove("entity");
        MDC.remove("note");
    }


    /**
     * The migration for my private usage to fix current collection state after changing attribute.
     * There is no change attribute mechanism yet, so it will be done by this migration.
     */
    @MigrationUnit(id = 2)
    public void fixDiaryDateAttributeName(MigrationContext context)
    {
        MDC.put("migration", this.getClass().getSimpleName());

        ApplicationContext applicationContext = (ApplicationContext)context.getVariable(APPLICATION_CONTEXT);
        Database db = applicationContext.getBean(Database.class);
        EntityService entityService = applicationContext.getBean(EntityService.class);
        NoteService noteService = applicationContext.getBean(NoteService.class);

        Entity diary = entityService.getByName("diary");
        String collectionName = noteService.getCollectionName(diary.getName());

        MDC.put("entity", diary.getName());

        String sortField = String.format("%s.%s", NoteConstants.ATTRIBUTES, diary.getKeyAttribute());
        List<Document> notes = db.getDocuments(collectionName, sortField);
        for (Document note : notes)
        {
            MDC.put("note", note.get(MONGO_ID).toString());

            // We can use this migration only if the previous one has successfully changed attributes field
            Object attributesField = note.get(NoteConstants.ATTRIBUTES);
            if (!(attributesField instanceof Document))
                continue;

            Document attributes = (Document)note.get(NoteConstants.ATTRIBUTES);

            // Nothing to change
            if (!attributes.containsKey("date"))
                continue;

            Object date = attributes.get("date");
            Object dateRequired = attributes.get("date-required");

            if (dateRequired == null && date != null)
                attributes.put("date-required", date);
            attributes.remove("date");

            note.put(NoteConstants.ATTRIBUTES, attributes);

            db.updateDocument(collectionName, note);

            log.info("Date attribute has been changed from 'date' to 'date-required'");
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

        ApplicationContext applicationContext = (ApplicationContext)context.getVariable(APPLICATION_CONTEXT);
        Database db = applicationContext.getBean(Database.class);
        EntityService entityService = applicationContext.getBean(EntityService.class);
        NoteService noteService = applicationContext.getBean(NoteService.class);
        final String FAVOURITE = "favourite";

        Entity[] entities = entityService.getAll();
        for (Entity entity : entities)
        {
            MDC.put("entity", entity.getName());

            String collectionName = noteService.getCollectionName(entity.getName());
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

                    log.info("The 'favourite' attribute has been renamed to 'favorite'");
                }
            }
        }

        MDC.remove("migration");
        MDC.remove("entity");
        MDC.remove("note");
    }
}

