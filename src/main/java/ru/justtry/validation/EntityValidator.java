package ru.justtry.validation;

import static ru.justtry.shared.AttributeConstants.ATTRIBUTES_COLLECTION;
import static ru.justtry.shared.ErrorMessages.NOT_ALL_ATTRIBUTES_FOUND;
import static ru.justtry.shared.ErrorMessages.NOT_ALL_COMPARED_ATTRIBUTES_FOUND;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import ru.justtry.database.Database;
import ru.justtry.database.SortInfo;
import ru.justtry.database.SortInfo.Direction;
import ru.justtry.metainfo.AttributeService;
import ru.justtry.metainfo.Entity;
import ru.justtry.shared.EntityConstants;
import ru.justtry.shared.ErrorMessages;

@Component
public class EntityValidator implements Validator
{
    @Autowired
    private AttributeService attributeService;
    @Autowired
    private Database database;

    @Override
    public void validate(Object object, String name)
    {
        Entity entity = (Entity)object;

        if (Strings.isNullOrEmpty(entity.getName()))
            throw new IllegalArgumentException(ErrorMessages.getIsNotSet("Name"));

        if (entity.getAttributes() == null || entity.getAttributes().size() < 1)
            throw new IllegalArgumentException("Entity should have at least 1 attribute");

        if (Strings.isNullOrEmpty(entity.getKeyAttribute()))
            throw new IllegalArgumentException(ErrorMessages.getIsNotSet("keyAttribute"));
        if (attributeService.getByName(entity.getKeyAttribute()) == null)
            throw new IllegalArgumentException("Attribute specified as ket attribute is not found");

        if (entity.getSortAttribute() != null && attributeService.getByName(entity.getSortAttribute()) == null)
            throw new IllegalArgumentException("Attribute specified as sort attribute is not found");

        if (entity.getSortAttribute() != null && entity.getSortDirection() == null)
            throw new IllegalArgumentException(ErrorMessages.getIsNotInPredefinedValues("sortDirection"));

        if (entity.getSortDirection() != null)
        {
            Direction direction = SortInfo.Direction.get(entity.getSortDirection());
            if (direction == null)
                throw new IllegalArgumentException(ErrorMessages.getIsNotInPredefinedValues("sortDirection"));
        }

        if (entity.getComparedAttributes().size() == 0)
            throw new IllegalArgumentException(ErrorMessages.getIsNotSet("comparedAttributes"));
        try
        {
            int actualCount = database.getDocuments(ATTRIBUTES_COLLECTION,
                    entity.getComparedAttributes(), EntityConstants.NAME).size();
            if (actualCount != entity.getComparedAttributes().size())
                throw new IllegalArgumentException(NOT_ALL_COMPARED_ATTRIBUTES_FOUND);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(NOT_ALL_COMPARED_ATTRIBUTES_FOUND);
        }

        if (database.isEntityExist(name))
            throw new IllegalArgumentException("Collection with this name already exists");

        try
        {
            int actualCount = database.getDocuments(ATTRIBUTES_COLLECTION,
                    entity.getAttributes(), EntityConstants.NAME).size();
            if (actualCount != entity.getAttributes().size())
                throw new IllegalArgumentException(NOT_ALL_ATTRIBUTES_FOUND);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(NOT_ALL_ATTRIBUTES_FOUND);
        }
    }
}
