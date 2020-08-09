package ru.justtry.validation;

import static ru.justtry.shared.ErrorMessages.NOT_ALL_ATTRIBUTES_FOUND;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import ru.justtry.database.Database;
import ru.justtry.metainfo.Entity;
import ru.justtry.rest.AttributesController;
import ru.justtry.shared.ErrorMessages;

@Component
public class EntityValidator implements Validator
{
    @Autowired
    private AttributesController attributesController;
    @Autowired
    private Database database;

    @Override
    public void validate(Object object, String collectionName)
    {
        Entity entity = (Entity)object;

        if (Strings.isNullOrEmpty(entity.getCollection()))
            throw new IllegalArgumentException(ErrorMessages.getIsNotSet("Name"));

        if (entity.getAttributes() == null || entity.getAttributes().size() < 1)
            throw new IllegalArgumentException("Entity should have at least 1 attribute");

        if (database.isEntityExist(collectionName))
            throw new IllegalArgumentException("Collection already exists");

        try
        {
            int actualCount = database.getDocuments(attributesController.getCollectionName(), entity.getAttributes()).size();
            if (actualCount != entity.getAttributes().size())
                throw new IllegalArgumentException(NOT_ALL_ATTRIBUTES_FOUND);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(NOT_ALL_ATTRIBUTES_FOUND);
        }
    }
}
