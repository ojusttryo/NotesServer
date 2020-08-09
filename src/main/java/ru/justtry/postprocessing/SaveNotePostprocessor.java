package ru.justtry.postprocessing;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.justtry.database.Database;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.Attribute.Type;
import ru.justtry.notes.Note;

@Component
public class SaveNotePostprocessor extends Postprocessor
{
    @Autowired
    private Database database;

    @Override
    public void process(Object object, String entity)
    {
        Note note = (Note)object;
        Map<String, Attribute> attributes = Arrays.stream(database.getAttributes(entity))
                .collect(Collectors.toMap(attr -> ((Attribute)attr).getName(), attr -> (Attribute)attr));

        for (String attributeName : attributes.keySet())
        {
            Attribute.Type type = Attribute.Type.get(attributes.get(attributeName).getType());

            if (type == Type.FILE)
            {
                String fileId = (String)note.getAttributes().get(attributeName);
                if (fileId != null && fileId.length() > 0)
                    database.linkFilesAndNote(note.getId(), attributeName, Arrays.asList(fileId));
            }
        }
    }
}
