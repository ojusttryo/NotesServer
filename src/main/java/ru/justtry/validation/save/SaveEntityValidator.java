package ru.justtry.validation.save;

import static ru.justtry.shared.AttributeConstants.ATTRIBUTES_COLLECTION;
import static ru.justtry.shared.ErrorMessages.NOT_ALL_ATTRIBUTES_FOUND;
import static ru.justtry.shared.ErrorMessages.NOT_ALL_COMPARED_ATTRIBUTES_FOUND;
import static ru.justtry.shared.ErrorMessages.NOT_ALL_VISIBLE_ATTRIBUTES_FOUND;

import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import lombok.RequiredArgsConstructor;
import ru.justtry.database.Database;
import ru.justtry.database.sort.Direction;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.AttributeService;
import ru.justtry.metainfo.Entity;
import ru.justtry.metainfo.dictionary.Type;
import ru.justtry.shared.EntityConstants;
import ru.justtry.shared.ErrorMessages;

@Component
@RequiredArgsConstructor
public class SaveEntityValidator implements SaveValidator
{
    private final AttributeService attributeService;
    private final Database database;


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
        Attribute keyAttribute = attributeService.getByName(entity.getKeyAttribute());
        if (keyAttribute == null)
            throw new IllegalArgumentException("Attribute specified as key attribute is not found");
        Type keyAttrType = keyAttribute.getTypeAsEnum();
        if (keyAttrType != Type.SELECT && !Type.isNumericType(keyAttrType) && !Type.isTextType(keyAttrType))
            throw new IllegalArgumentException("Key attribute should be textual, numeric or select type");

        if (entity.getSortAttribute() == null)
            throw new IllegalArgumentException("No sort attribute specified");
        if (attributeService.getByName(entity.getSortAttribute()) == null)
            throw new IllegalArgumentException("Attribute specified as sort attribute is not found");

        if (entity.getSortDirection() == null)
            throw new IllegalArgumentException(ErrorMessages.getIsNotInPredefinedValues("sort direction"));
        if (entity.getSortDirection() != null)
        {
            Direction direction = Direction.get(entity.getSortDirection());
            if (direction == null)
                throw new IllegalArgumentException(ErrorMessages.getIsNotInPredefinedValues("sort direction"));
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

        if (entity.getVisibleAttributes().size() == 0)
            throw new IllegalArgumentException(ErrorMessages.getIsNotSet("visibleAttributes"));
        try
        {
            int actualCount = database.getDocuments(ATTRIBUTES_COLLECTION,
                    entity.getVisibleAttributes(), EntityConstants.NAME).size();
            if (actualCount != entity.getVisibleAttributes().size())
                throw new IllegalArgumentException(NOT_ALL_VISIBLE_ATTRIBUTES_FOUND);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(NOT_ALL_VISIBLE_ATTRIBUTES_FOUND);
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
