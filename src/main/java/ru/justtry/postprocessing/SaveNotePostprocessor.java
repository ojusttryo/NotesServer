package ru.justtry.postprocessing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ru.justtry.database.Database;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.dictionary.Type;
import ru.justtry.metainfo.AttributeService;
import ru.justtry.notes.Note;

@Component
@RequiredArgsConstructor
public class SaveNotePostprocessor
{
    private final Database database;
    private final AttributeService attributeService;


    public void process(Note note, @Nullable Note oldNote, String entityName)
    {
        Map<String, Attribute> attributes = attributeService.getAttributesAsMap(entityName);
        for (String attributeName : attributes.keySet())
        {
            Type type = attributes.get(attributeName).getTypeAsEnum();

            if (Type.isFile(type))
            {
                String oldFileId = oldNote == null ? null : (String)oldNote.getAttributes().get(attributeName);
                String newFileId = (String)note.getAttributes().get(attributeName);

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
                ArrayList<String> newFilesList = note.getAttributes().get(attributeName) == null ? new ArrayList<>()
                        : (ArrayList<String>)note.getAttributes().get(attributeName);
                ArrayList<String> oldFilesList = oldNote == null || oldNote.getAttributes().get(attributeName) == null
                        ? new ArrayList<>() : (ArrayList<String>)oldNote.getAttributes().get(attributeName);

                Set<String> filesToLink = new HashSet<>(newFilesList);
                Set<String> filesToUnlink = new HashSet<>(oldFilesList);
                Set<String> intersection = filesToLink.stream().filter(filesToUnlink::contains).collect(Collectors.toSet());
                filesToLink.removeAll(intersection);
                filesToUnlink.removeAll(intersection);

                database.linkFilesAndNote(note.getId(), attributeName, filesToLink);
                database.unlinkFilesAndNote(note.getId(), attributeName, filesToUnlink);
            }
        }
    }
}
