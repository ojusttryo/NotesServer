package ru.justtry.postprocessing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import ru.justtry.database.Database;
import ru.justtry.mappers.AttributeMapper;
import ru.justtry.mappers.EntityMapper;
import ru.justtry.mappers.NoteMapper;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.Attribute.Type;
import ru.justtry.metainfo.AttributeService;
import ru.justtry.notes.Note;
import ru.justtry.rest.NotesController;
import ru.justtry.shared.Utils;

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
    @Autowired
    private Utils utils;
    @Autowired
    private AttributeService attributeService;

    public void process(Note note, Note oldNote, String entityName)
    {
        Map<String, Attribute> attributes = attributeService.getAttributesAsMap(entityName);
        for (String attributeName : attributes.keySet())
        {
            Attribute.Type type = attributes.get(attributeName).getTypeAsEnum();

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

            if (Type.isMultiFile(type))
            {
                ArrayList<String> noteImages = note.getAttributes().get(attributeName) == null ? new ArrayList<>()
                        : (ArrayList<String>)note.getAttributes().get(attributeName);
                ArrayList<String> oldNoteImages = oldNote == null || oldNote.getAttributes().get(attributeName) == null
                        ? new ArrayList<>() : (ArrayList<String>)oldNote.getAttributes().get(attributeName);

                Set<String> newImages = new HashSet<>(noteImages);
                Set<String> oldImages = new HashSet<>(oldNoteImages);
                Set<String> intersection = newImages.stream().filter(oldImages::contains).collect(Collectors.toSet());
                newImages = noteImages.stream().filter(x -> !intersection.contains(x)).collect(Collectors.toSet());
                oldImages = oldImages.stream().filter(x -> !intersection.contains(x)).collect(Collectors.toSet());

                database.linkFilesAndNote(note.getId(), attributeName, newImages);
                database.unlinkFilesAndNote(note.getId(), attributeName, oldImages);
            }
        }
    }
}
