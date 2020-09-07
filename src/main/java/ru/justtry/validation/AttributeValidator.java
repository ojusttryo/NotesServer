package ru.justtry.validation;

import static ru.justtry.shared.AttributeConstants.ATTRIBUTES_COLLECTION;
import static ru.justtry.shared.ErrorMessages.NAME_IS_DUPLICATED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.justtry.database.Database;
import ru.justtry.mappers.AttributeMapper;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.Attribute.ImageSize;
import ru.justtry.metainfo.Attribute.Method;
import ru.justtry.metainfo.Attribute.Type;
import ru.justtry.shared.ErrorMessages;
import ru.justtry.shared.Identifiable;

@Component
public class AttributeValidator implements Validator
{

    private static Pattern namePattern = Pattern.compile("\\A[a-zA-Z]{1}[0-9a-zA-Z\\-]*\\Z");
    private static Pattern positiveIntegerPattern = Pattern.compile("\\d+");

    @Autowired
    private Database database;
    @Autowired
    private AttributeMapper attributeMapper;

    @Override
    public void validate(Object object, String collectionName)
    {
        Attribute attribute = (Attribute)object;

        // Some properties are supposed to be sent from select and stuff, i.e. they should be without any spaces.
        // In this cases no trim() method called. If there is a mistake in them, no corrections.

        Attribute attributeWithSameId = null;
        if (attribute.getId() != null)
        {
            attributeWithSameId = (Attribute)attributeMapper.getObject(
                    database.getDocument(ATTRIBUTES_COLLECTION, attribute.getId()));
        }

        attribute.setName(attribute.getName().trim());
        if (attribute.getName().length() == 0 || !namePattern.matcher(attribute.getName()).find())
            throw new IllegalArgumentException("Name should start with latin letter and contains latin letters,"
                    + " numbers and dashes");

        Document attributeWithSameNameDoc = database.getAttribute(attribute.getName());
        Identifiable attributeWithSameName = attributeWithSameNameDoc == null ? null
                : attributeMapper.getObject(attributeWithSameNameDoc);

        if (attribute.getId() == null && attributeWithSameName != null)
            throw new IllegalArgumentException(NAME_IS_DUPLICATED);

        if (attributeWithSameId != null && attributeWithSameName != null &&
                !attributeWithSameId.getId().contentEquals(attributeWithSameName.getId()))
            throw new IllegalArgumentException(NAME_IS_DUPLICATED);

        if (attributeWithSameId != null && !attributeWithSameId.getName().contentEquals(attribute.getName()))
            throw new IllegalArgumentException("Name should not be changed");

        attribute.setTitle(attribute.getTitle().trim());
        if (attribute.getTitle().length() == 0)
            throw new IllegalArgumentException(ErrorMessages.getIsNotSet("Title"));

        if (attribute.getMethod() == null)
            throw new IllegalArgumentException(ErrorMessages.getIsNotSet("Method"));

        Attribute.Method method = Attribute.Method.get(attribute.getMethod());
        if (method == null)
            throw new IllegalArgumentException(ErrorMessages.getIsNotInPredefinedValues("method"));

        if (attribute.getType() == null)
            throw new IllegalArgumentException(ErrorMessages.getIsNotSet("Type"));

        Attribute.Type type = attribute.getTypeAsEnum();
        if (type == null)
            throw new IllegalArgumentException(ErrorMessages.getIsNotInPredefinedValues("type"));

        if (method == Method.AVG && type != Type.NUMBER)
            throw new IllegalStateException("Method avg should be used only for numeric type of attribute");

        Double min = attribute.getMin();
        Double max = attribute.getMax();
        if (max != null && min != null && min > max)
            throw new IllegalArgumentException("max should be greater or equals to min");

        if ((Type.isTextType(type)))
        {
            if (min != null && min < 0)
                throw new IllegalStateException("min should be greater or equal to zero with text attributes");
            if (max != null && max < 0)
                throw new IllegalStateException("max should be greater or equal to zero with text attributes");
        }

        attribute.setSelectOptions(attribute.getSelectOptions() == null ? new ArrayList<>() :
                attribute.getSelectOptions().stream().map(String::trim).collect(Collectors.toList()));

        if (attribute.getDefaultValue() != null)
        {
            attribute.setDefaultValue(attribute.getDefaultValue());

            switch (type)
            {
            case URL:
            case USER_DATE:
            case SAVE_TIME:
            case UPDATE_TIME:
            case USER_TIME:
            case FILE:
                break;
            case TEXT:
            case TEXT_AREA:
                if (min != null && attribute.getDefaultValue().length() < min)
                    throw new IllegalArgumentException("defaultValue should be at least " + min + " characters");
                if (max != null && attribute.getDefaultValue().length() > max)
                    throw new IllegalArgumentException("defaultValue should be less than " + max + " characters");
                break;
            case NUMBER:
            case INC:
                Double defaultValue = null;
                try
                {
                    defaultValue = Double.parseDouble(attribute.getDefaultValue());
                }
                catch (Exception e)
                {
                    throw new IllegalArgumentException(ErrorMessages.getShouldBeNumber("defaultValue"));
                }
                if (min != null && defaultValue < min)
                    throw new IllegalArgumentException("defaultValue should be not less than " + min);
                if (max != null && defaultValue > max)
                    throw new IllegalArgumentException("defaultValue should be not greater than " + max);
                break;
            case SELECT:
                if (attribute.getSelectOptions().stream().allMatch(option -> !option.contentEquals(attribute.getDefaultValue())))
                    throw new IllegalArgumentException("defaultValue is not one of the options for select");
                break;
            case MULTI_SELECT:
                List<String> defaultOptions = Arrays.stream(attribute.getDefaultValue().split(";"))
                        .map(String::trim).collect(Collectors.toList());
                for (String defaultOption : defaultOptions)
                {
                    if (attribute.getSelectOptions().stream().allMatch(option -> !option.contentEquals(defaultOption)))
                        throw new IllegalArgumentException("defaultValue is not one of the options for multiselect");
                }
                break;
            case CHECKBOX:
                if (!attribute.getDefaultValue().contentEquals("true")
                        && !attribute.getDefaultValue().contentEquals("false"))
                    throw new IllegalArgumentException("defaultValue should be true or false");
                break;
            }
        }

        Double step = null;
        if (attribute.getStep() != null)
        {
            attribute.setStep(attribute.getStep());
            try
            {
                step = Double.parseDouble(attribute.getStep());
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException(ErrorMessages.getShouldBeNumber("step"));
            }
        }

        if (type == Type.INC)
        {
            if (step == null)
                throw new IllegalStateException("step should be set for attribute of inc type");
            if (max != null && min != null && (max - min) < step)
                throw new IllegalArgumentException("step should be at least greater than (max - min)");
        }

        if (type == Type.TEXT_AREA && attribute.getLinesCount() == null)
            throw new IllegalArgumentException(ErrorMessages.getIsNotSet("linesCount"));

        if (attribute.getLinesCount() != null && attribute.getLinesCount() < 1)
            throw new IllegalArgumentException("linesCount should be greater or equal to 1");

        if (Attribute.Alignment.get(attribute.getAlignment()) == null)
            throw new IllegalArgumentException(ErrorMessages.getIsNotInPredefinedValues("alignment"));

        if (Attribute.Type.isTimestampType(type) && attribute.getDateFormat() == null)
            throw new IllegalArgumentException(ErrorMessages.getIsNotSet("dateFormat"));

        if (attribute.getRegex() != null)
        {
            try
            {
                Pattern.compile(attribute.getRegex());
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("regex is incorrect");
            }
        }

        if (Type.isSelectType(type) && (attribute.getSelectOptions() == null || attribute.getSelectOptions().size() < 1))
            throw new IllegalStateException("No select options are set");

        if ((type == Type.INC || type == Type.SELECT) && attribute.getEditableInTable() == null)
            throw new IllegalArgumentException(ErrorMessages.getIsNotSet("editableInTable"));

        Integer imgSize = attribute.getImagesSize();
        if (type == Type.GALLERY && (imgSize == null || ImageSize.get(imgSize) == null))
            throw new IllegalArgumentException((ErrorMessages.getIsNotInPredefinedValues("images size")));

        if (type == Type.DELIMITED_TEXT && (attribute.getDelimiter() == null || attribute.getDelimiter().length() == 0))
            throw new IllegalArgumentException(ErrorMessages.getIsNotSet("delimiter"));
    }
}
