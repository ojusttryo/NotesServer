package ru.justtry.postprocessing;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ru.justtry.database.Database;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.dictionary.Type;
import ru.justtry.metainfo.AttributeService;
import ru.justtry.notes.Note;

@Component
@RequiredArgsConstructor
public class DeleteNotePostprocessor
{
    private final Database database;
    private final AttributeService attributeService;


    public void process(Note note, String entityName)
    {
        Map<String, Attribute> attributes = attributeService.getAttributesAsMap(entityName);
        for (String attributeName : attributes.keySet())
        {
            Type type = attributes.get(attributeName).getTypeAsEnum();

            if (Type.isFile(type))
            {
                String fileId = (String)note.getAttributes().get(attributeName);
                if (fileId != null && fileId.length() > 0)
                    database.unlinkFilesAndNote(note.getId(), attributeName);
            }

            if (Type.isMultiFile(type))
            {
                ArrayList<String> noteFiles = note.getAttributes().get(attributeName) == null ? new ArrayList<>()
                        : (ArrayList<String>)note.getAttributes().get(attributeName);

                database.unlinkFilesAndNote(note.getId(), attributeName, noteFiles);
            }
        }
    }
}
