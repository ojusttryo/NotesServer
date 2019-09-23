package ru.justtry.attributes;

import org.bson.Document;
import org.springframework.stereotype.Component;
import ru.justtry.shared.Validator;

@Component
public class EntityValidator implements Validator
{
    @Override
    public void validate(Object entity)
    {

    }

    @Override
    public void validate(Document document)
    {

    }
}
