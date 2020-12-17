package ru.justtry.validation.delete;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.Entity;
import ru.justtry.metainfo.EntityService;


@Component
public class DeleteAttributeValidator implements DeleteValidator
{
    @Autowired
    private EntityService entityService;


    @Override
    public void validate(Object object, String collectionName)
    {
        Attribute attribute = (Attribute)object;

        Entity[] usage = entityService.getByAttribute(attribute.getName());
        if (usage.length > 0)
        {
            List<String> usageList = Arrays.stream(usage).map(Entity::getName).collect(Collectors.toList());
            throw new IllegalStateException("Attribute is used in entities: " + String.join(", ", usageList));
        }
    }
}
