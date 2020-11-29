package ru.justtry.database.migrations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.ojusttryo.migmong.migration.MigrationContext;
import com.github.ojusttryo.migmong.migration.annotations.Migration;
import com.github.ojusttryo.migmong.migration.annotations.MigrationUnit;
import com.google.common.base.Strings;

import ru.justtry.database.Database;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.AttributeService;
import ru.justtry.metainfo.Entity;
import ru.justtry.metainfo.EntityService;
import ru.justtry.notes.Note;
import ru.justtry.notes.NoteService;

/**
 * Removes files that were left after deleting from notes due to bugs.
 */
@Migration
public class V0_1_2__cleanFiles
{
    final static Logger logger = LogManager.getLogger(V0_1_2__cleanFiles.class);

    @MigrationUnit(id = 1)
    public void cleanFiles(MigrationContext context)
    {
        Database db = context.getApplicationContext().getBean(Database.class);
        EntityService entityService = context.getApplicationContext().getBean(EntityService.class);
        AttributeService attributeService = context.getApplicationContext().getBean(AttributeService.class);
        NoteService noteService = context.getApplicationContext().getBean(NoteService.class);

        Set<String> usedIds = new HashSet<>();
        Entity[] entities = entityService.getAll();
        for (Entity entity : entities)
        {
            Attribute[] attributes = attributeService.get(entity.getName());
            Note[] notes = noteService.get(entity.getName());

            for (Note note : notes)
            {
                for (Attribute attribute : attributes)
                {
                    if (Attribute.Type.isFile(attribute.getTypeAsEnum()))
                    {
                        String id = note.getAttributes().get(attribute.getName()).toString();
                        if (!Strings.isNullOrEmpty(id))
                            usedIds.add(id);
                    }

                    if (Attribute.Type.isMultiFile(attribute.getTypeAsEnum()))
                    {
                        Object value = note.getAttributes().get(attribute.getName());
                        if (value != null)
                        {
                            List<String> ids = (ArrayList<String>)value;
                            usedIds.addAll(ids);
                        }
                    }
                }
            }
        }

        int count = db.unlinkAllFilesExcept(usedIds);
        logger.info(String.format("%d files have been unlinked and prepared for deleting", count));
    }
}
