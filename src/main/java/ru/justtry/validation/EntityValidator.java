package ru.justtry.validation;

import com.google.common.base.Strings;
import org.springframework.stereotype.Component;
import ru.justtry.metainfo.Entity;
import ru.justtry.validation.Validator;

import static ru.justtry.shared.ErrorMessages.ATTRIBUTES_SIZE_INCORRECT;
import static ru.justtry.shared.ErrorMessages.NAME_IS_NOT_SET;

@Component
public class EntityValidator implements Validator
{
    @Override
    public void validate(Object object)
    {
        Entity entity = (Entity)object;

        checkName(entity);
        checkAttributes(entity);

        // TODO check attributes exists in DB
    }

    private void checkName(Entity entity)
    {
        if (Strings.isNullOrEmpty(entity.getName()))
            throw new IllegalArgumentException(NAME_IS_NOT_SET);
    }

    private void checkAttributes(Entity entity)
    {
        if (entity.getAttributes() == null || entity.getAttributes().size() < 1)
            throw new IllegalArgumentException(ATTRIBUTES_SIZE_INCORRECT);
    }
}
