package ru.justtry.notes;

import org.bson.Document;
import org.springframework.stereotype.Component;
import ru.justtry.attributes.Attribute;
import ru.justtry.attributes.AttributeMapper;
import ru.justtry.database.Database;
import ru.justtry.shared.Validator;

import javax.inject.Inject;
import java.util.Map;

import static ru.justtry.attributes.AttributeConstants.*;

@Component
public class NoteValidator implements Validator
{
    @Inject
    private Database database;
    @Inject
    private AttributeMapper attributeMapper;

    @Override
    public void validate(Object object)
    {
        Note note = (Note)object;

        validateFolder(note);
        validateAttributes(note);
    }

    private void validateFolder(Note note)
    {
        if (note.getFolderId() != null)
        {
            // TODO check if database contains such folder
        }
    }

    private void validateAttributes(Note note)
    {
        for (Map.Entry<String, Object> value : note.getAttributes())
        {
            Attribute attribute = (Attribute)database.getObject(ATTRIBUTES_COLLECTION, attributeMapper, value.getKey());

            checkExists(value, attribute);
            checkByRegex(value, attribute);
            checkRange(value, attribute);
        }
    }

    private void checkExists(Map.Entry<String, Object> value, Attribute attribute)
    {
        if (attribute.getRequired() != null && attribute.getRequired() == true)
        {
            if (value.getValue() == null)
                throwIllegalArg("The value of the %s must be set", value.getKey());
        }
    }

    private void checkRange(Map.Entry<String, Object> value, Attribute attribute)
    {
        String min = attribute.getMinValue();
        String max = attribute.getMaxValue();

        if (min == null && max == null)
            return;

        if (attribute.getType() == TEXT || attribute.getType() == TEXTAREA)
        {
            String string = (String)value.getValue();

            if (min != null)
            {
                Integer minLength = Integer.parseInt(min);
                if (string.length() < minLength)
                    throwIllegalArg("The length of the %s is too small", value.getKey());
            }
            if (max != null)
            {
                Integer maxLength = Integer.parseInt(max);
                if (string.length() > maxLength)
                    throwIllegalArg("The length of the %s is too big", value.getKey());
            }
        }
        else if (attribute.getType() == INT || attribute.getType() == FLOAT)
        {
            Double number = (Double)value.getValue();

            if (min != null)
            {
                Double minValue = Double.parseDouble(min);
                if (number < minValue)
                    throwIllegalArg("The value of the %s is less than min value %s", value.getKey(), min);
            }
            if (max != null)
            {
                Double maxValue = Double.parseDouble(max);
                if (number > maxValue)
                    throwIllegalArg("The value of the %s is bigger than max value %s", value.getKey(), max);
            }
        }
    }

    private void checkByRegex(Map.Entry<String, Object> value, Attribute attribute)
    {
        if (attribute.getRegex() == null)
            return;

        String string = (String)value.getValue();
        if (!string.matches(attribute.getRegex()))
            throwIllegalArg("The value %s doesn't match regex %s", string, attribute.getRegex());
    }

    private void throwIllegalArg(String message, Object... args)
    {
        throw new IllegalArgumentException(String.format(message, args));
    }
}
