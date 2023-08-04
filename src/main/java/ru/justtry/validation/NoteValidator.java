package ru.justtry.validation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.justtry.database.Database;
import ru.justtry.mappers.AttributeMapper;
import ru.justtry.mappers.EntityMapper;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.Attribute.Type;
import ru.justtry.metainfo.AttributeService;
import ru.justtry.notes.Note;
import ru.justtry.shared.Utils;

@Component
public class NoteValidator implements Validator
{
    @Autowired
    private Database database;
    @Autowired
    private AttributeMapper attributeMapper;
    @Autowired
    private EntityMapper entityMapper;
    @Autowired
    private Utils utils;
    @Autowired
    private AttributeService attributeService;

    @Override
    public void validate(Object object, String collectionName)
    {
        if (!database.isEntityExist(collectionName))
            throw new IllegalArgumentException("Unknown collection " + collectionName);

        Note note = (Note)object;
        Map<String, Attribute> attributes = attributeService.getAttributesAsMap(collectionName);

        for (String name : attributes.keySet())
        {
            Attribute.Type type = attributes.get(name).getTypeAsEnum();

            // validate length of file ID (32)
        }

        // TODO set save and updae date

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
        // TODO: здесь надо получить все атрибуты для заметки и проверить уже по полному списку, а не тому, что пришло
        // с клиента


        if (true)
            return;

        for (String id : note.getAttributes().keySet())
        {
           // Attribute attribute = (Attribute)database.getDocument(ATTRIBUTES_COLLECTION, id);

//            checkExists(value, attribute);
//            checkByRegex(value, attribute);
//            checkRange(value, attribute);
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
        Double min = attribute.getMin();
        Double max = attribute.getMax();

        if (min == null && max == null)
            return;

        if (Type.isTextType(attribute.getType()))
        {
            String string = (String)value.getValue();

            if (min != null && string.length() < min)
                throwIllegalArg("The length of the %s is too small", value.getKey());
            if (max != null && string.length() > max)
                throwIllegalArg("The length of the %s is too big", value.getKey());
        }
        else if (Type.isNumericType(attribute.getType()))
        {
            Double number = (Double)value.getValue();

            if (min != null && number < min)
                throwIllegalArg("The value of the %s is less than min value %s", value.getKey(), min);
            if (max != null && number > max)
                throwIllegalArg("The value of the %s is bigger than max value %s", value.getKey(), max);
        }
    }

    private void checkByRegex(Map.Entry<String, Object> value, Attribute attribute)
    {
        if (attribute.getRegex() == null)
            return;

        String string = value.getValue().toString();
        if (!string.matches(attribute.getRegex()))
            throwIllegalArg("The value %s doesn't match regex %s", string, attribute.getRegex());
    }

    private void throwIllegalArg(String message, Object... args)
    {
        throw new IllegalArgumentException(String.format(message, args));
    }
}
