package ru.justtry.postprocessing;

import static ru.justtry.shared.AttributeConstants.ATTRIBUTES_COLLECTION;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import ru.justtry.database.Database;
import ru.justtry.mappers.AttributeMapper;
import ru.justtry.mappers.EntityMapper;
import ru.justtry.mappers.NoteMapper;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.Attribute.Type;
import ru.justtry.metainfo.Entity;
import ru.justtry.notes.Note;
import ru.justtry.rest.NotesController;
import ru.justtry.shared.Identifiable;

@Component
public class SaveNotePostprocessor
{
    @Autowired
    private Database database;
    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private AttributeMapper attributeMapper;
    @Autowired
    private EntityMapper entityMapper;
    @Autowired
    @Lazy
    private NotesController notesController;

    public void process(Note note, Note oldNote, String entityName)
    {
        Entity entity = (Entity)entityMapper.getObject(database.getEntity(entityName));
        List<Document> documents = database.getDocuments(ATTRIBUTES_COLLECTION, entity.getAttributes());
        Identifiable[] objects = attributeMapper.getObjects(documents);
        Map<String, Attribute> attributes = Arrays.stream(objects)
                .collect(Collectors.toMap(attr -> ((Attribute)attr).getName(), attr -> (Attribute)attr));

        for (String attributeName : attributes.keySet())
        {
            Attribute.Type type = Attribute.Type.get(attributes.get(attributeName).getType());

            if (Type.isFile(type))
            {
                String oldFileId = oldNote == null ? null : (String)oldNote.getAttributes().get(attributeName);
                String newFileId = (String)note.getAttributes().get(attributeName);

                // TODO check here if new file exists

                boolean fileIsDeleted = (newFileId == null && oldFileId != null);
                boolean fileIsChanged = (newFileId != null && oldFileId != null && !newFileId.contentEquals(oldFileId));
                boolean fileIsAdded = (oldFileId == null && newFileId != null);
                if (fileIsDeleted || fileIsChanged)
                    database.unlinkFilesAndNote(note.getId(), attributeName);
                if (fileIsAdded || fileIsChanged)
                    database.linkFilesAndNote(note.getId(), attributeName, Arrays.asList(newFileId));
            }
        }
    }
}
