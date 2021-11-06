package ru.justtry.validation.save;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import lombok.RequiredArgsConstructor;
import ru.justtry.database.Database;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.AttributeService;
import ru.justtry.notes.Note;

@Component
@RequiredArgsConstructor
public class SaveNoteValidator implements SaveValidator
{
    private final Database database;
    private final AttributeService attributeService;


    @Override
    public void validate(Object object, String entityName)
    {
        if (!database.isEntityExist(entityName))
            throw new IllegalArgumentException("Unknown entity " + entityName);

        if (object == null)
            throw new IllegalArgumentException("Note is absent");

        Note note = (Note)object;
        if (note.getId() != null && note.getId().length() != 24)
            throwIllegalArg("Note id should be 24 characters length");

        Attribute[] attributes = attributeService.get(entityName);
        for (Attribute attribute : attributes)
        {
            Object value = note.getAttributes().get(attribute.getName());
            Double max = attribute.getMax();
            Double min = attribute.getMin();
            String regex = attribute.getRegex();

            if (value == null)
            {
                if (attribute.getRequired())
                    throwIllegalArg("Value of the '%s' is not set", attribute.getTitle());
                else
                    continue;
            }

            switch (attribute.getTypeAsEnum())
            {
            case NUMBER:
            case INC:
                if (!(value instanceof Double || value instanceof Integer))
                    throwIllegalArg("Wrong type of the '%s'", attribute.getTitle());

                // No need to check 'step' - it's only for the interface
                Double valueDouble = Double.parseDouble(value.toString());
                if (min != null && valueDouble < min)
                {
                    throwIllegalArg("The value of the '%s' is less than min value '%s'",
                            attribute.getTitle(), min);
                }
                if (max != null && valueDouble > max)
                {
                    throwIllegalArg("The value of the '%s' is bigger than max value '%s'",
                            attribute.getTitle(), max);
                }
                break;

            case TEXT:
            case TEXT_AREA:
            case DELIMITED_TEXT:
                if (!(value instanceof String))
                    throwIllegalArg("Wrong type of the '%s'", attribute.getTitle());

                String valueStr = (String)value;

                if (attribute.getRequired() && valueStr.length() == 0)
                    throwIllegalArg("Value of the '%s' can not be empty", attribute.getTitle());

                if (max != null && valueStr.length() > max)
                    throwIllegalArg("Value of the '%s' is too long", attribute.getTitle());

                if (min != null && valueStr.length() < min)
                    throwIllegalArg("Value of the '%s' is too short", attribute.getTitle());

                if (regex != null && !valueStr.matches(regex))
                {
                    throwIllegalArg("Value of the '%s' doesn't match the pattern '%s'",
                            attribute.getTitle(), regex);
                }
                break;

            case SELECT:
                if (!(value instanceof String))
                    throwIllegalArg("Wrong type of the '%s'", attribute.getTitle());

                // It might need to check the empty value but it's expected to be an empty option in select
                String valueSelect = (String)value;
                if (attribute.getSelectOptions().stream().allMatch(option -> !option.split("=")[0].contentEquals(valueSelect)))
                {
                    throwIllegalArg("Value of the '%s' is not one of the options for select",
                            attribute.getTitle());
                }
                break;

            case MULTI_SELECT:
                if (!(value instanceof ArrayList))
                    throwIllegalArg("Wrong type of the '%s'", attribute.getTitle());

                ArrayList<String> valueMulti = (ArrayList<String>)value;
                for (String val : valueMulti)
                {
                    if (attribute.getSelectOptions().stream().allMatch(option -> !option.contentEquals(val)))
                    {
                        throwIllegalArg("Values of the '%s' are not in the option list for select",
                                attribute.getTitle());
                    }
                }
                break;

            case CHECKBOX:
                if (!(value instanceof Boolean))
                    throwIllegalArg("Wrong type of the '%s'", attribute.getTitle());
                break;

            case FILE:
            case IMAGE:
                if (!(value instanceof String))
                    throwIllegalArg("Wrong type of the '%s'", attribute.getTitle());

                String fileId = (String)value;

                if (Strings.isNullOrEmpty(fileId) || !database.isFileExists(fileId))
                    throwFileDoesNotExist(fileId, attribute.getTitle());

                Integer fileSize = database.getFileSize(fileId);
                if (max != null && (fileSize / 1024) > max)
                    throwIllegalArg("The file size for the '%s' is too big.", attribute.getTitle());
                if (min != null && (fileSize / 1024) < min)
                    throwIllegalArg("The file size for the '%s' is too small.", attribute.getTitle());
                break;

            case GALLERY:
            case FILES:
                if (!(value instanceof ArrayList))
                    throwIllegalArg("Wrong type of the '%s'", attribute.getTitle());

                ArrayList<String> valueFiles = (ArrayList<String>)value;

                for (String file : valueFiles)
                {
                    if (Strings.isNullOrEmpty(file) || !database.isFileExists(file))
                        throwFileDoesNotExist(file, attribute.getTitle());
                }

                for (String file :valueFiles)
                {
                    Integer size = database.getFileSize(file);
                    if (max != null && (size / 1024) > max)
                        throwIllegalArg("The file size for the '%s' is too big.", attribute.getTitle());
                    if (min != null && (size / 1024) < min)
                        throwIllegalArg("The file size for the '%s' is too small.", attribute.getTitle());
                }
                break;

            case URL:
                if (!(value instanceof String))
                    throwIllegalArg("Wrong type of the '%s'", attribute.getTitle());
                break;

            case USER_DATE:
                String valueDate = (String)value;
                if (!valueDate.matches("\\A\\d{4}-\\d{2}-\\d{2}\\Z"))
                    throwIllegalArg("Value of the '%s' doesn't fit pattern dd-MM-yyyy", attribute.getTitle());
                break;

            case USER_TIME:
                String valueTime = (String)value;
                if (!valueTime.matches("\\A\\d{2}:\\d{2}\\Z"))
                    throwIllegalArg("Value of the '%s' doesn't fit pattern HH:mm", attribute.getTitle());
                break;
            }
        }
    }


    private void throwIllegalArg(String message, Object... args)
    {
        throw new IllegalArgumentException(String.format(message, args));
    }


    private void throwFileDoesNotExist(String fileId, String attributeName)
    {

        String message = String.format("The file (id = %s) for the '%s' does not exist",
                Strings.isNullOrEmpty(fileId) ? " " : fileId, attributeName);
        throw new IllegalArgumentException(message);
    }

}
