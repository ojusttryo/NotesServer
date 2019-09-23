package ru.justtry.attributes;

import org.bson.Document;
import org.springframework.stereotype.Component;
import ru.justtry.attributes.Attribute;
import ru.justtry.shared.Validator;

@Component
public class AttributeValidator implements Validator
{
    @Override
    public void validate(Object attribute)
    {

    }

    @Override
    public void validate(Document document)
    {

    }
}
