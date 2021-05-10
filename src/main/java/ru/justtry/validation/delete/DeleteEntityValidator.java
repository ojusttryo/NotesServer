package ru.justtry.validation.delete;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ru.justtry.database.Database;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.dictionary.Type;
import ru.justtry.metainfo.AttributeService;
import ru.justtry.metainfo.Entity;
import ru.justtry.notes.NoteService;

@Component
@RequiredArgsConstructor
public class DeleteEntityValidator implements DeleteValidator
{
    private final AttributeService attributeService;
    private final Database database;
    private final NoteService noteService;


    @Override
    public void validate(Object object, String collectionName)
    {
        Entity entity = (Entity)object;

        Attribute[] attributes = Arrays.stream(attributeService.get(entity.getName()))
                .filter(x -> x.getTypeAsEnum() == Type.COMPARED_NOTES || x.getTypeAsEnum() == Type.NESTED_NOTES)
                .filter(x -> x.getEntity().equals(entity.getName()))
                .toArray(Attribute[]::new);

        if (attributes.length > 0)
        {
            List<String> usageList = Arrays.stream(attributes).map(Attribute::getName).collect(Collectors.toList());
            throw new IllegalStateException("Entity is used in attributes: " + String.join(", ", usageList));
        }

        Document entitiesInfo = database.getCollectionInfo(noteService.getCollectionName(entity.getName()));
        if (entitiesInfo != null)
        {
            long count = Long.parseLong(entitiesInfo.get("count").toString());
            if (count > 0)
                throw new IllegalStateException(String.format("There are some notes of %s", entity.getTitle()));
        }
    }
}
