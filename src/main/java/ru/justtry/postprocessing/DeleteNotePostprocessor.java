package ru.justtry.postprocessing;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.justtry.database.Database;
import ru.justtry.mappers.AttributeMapper;
import ru.justtry.mappers.EntityMapper;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.Attribute.Type;
import ru.justtry.metainfo.AttributeService;
import ru.justtry.notes.Note;
import ru.justtry.shared.Utils;

@Component
public class DeleteNotePostprocessor
{
    @Autowired
    private Database database;
    @Autowired
    private AttributeMapper attributeMapper;
    @Autowired
    private EntityMapper entityMapper;
    @Autowired
    private Utils utils;
    @Autowired
    private AttributeService attributeService;


    public void process(Note note, String entityName)
    {
        Map<String, Attribute> attributes = attributeService.getAttributesAsMap(entityName);
        for (String attributeName : attributes.keySet())
        {
            Attribute.Type type = attributes.get(attributeName).getTypeAsEnum();

            if (type == Type.FILE)
            {
                String fileId = (String)note.getAttributes().get(attributeName);
                if (fileId != null && fileId.length() > 0)
                    database.unlinkFilesAndNote(note.getId(), attributeName);
            }
        }
    }
}
