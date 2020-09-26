package ru.justtry.mappers;

import static ru.justtry.shared.AttributeConstants.*;
import static ru.justtry.shared.Constants.MONGO_ID;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import ru.justtry.metainfo.Attribute;
import ru.justtry.shared.Identifiable;

@Component
public class AttributeMapper extends Mapper
{
    @Override
    public Identifiable getObject(Document document)
    {
        Attribute attribute = new Attribute();

        attribute.setId(document.get(MONGO_ID).toString());
        attribute.setName(document.get(NAME).toString());
        attribute.setTitle(document.get(TITLE).toString());
        attribute.setAlignment(document.get(ALIGNMENT).toString());
        attribute.setImagesSize((Integer)document.get(IMAGES_SIZE));
        attribute.setLinesCount((Integer)document.get(LINES_COUNT));
        attribute.setType(document.get(TYPE).toString());
        attribute.setSelectOptions((List<String>)document.get(SELECT_OPTIONS));
        attribute.setMethod(getStringOrNull(document, METHOD));
        attribute.setMaxWidth(getStringOrNull(document, MAX_WIDTH));
        attribute.setMinWidth(getStringOrNull(document, MIN_WIDTH));
        attribute.setMaxHeight(getStringOrNull(document, MAX_HEIGHT));
        attribute.setMinHeight(getStringOrNull(document, MIN_HEIGHT));
        attribute.setMin((Double)document.get(MIN));
        attribute.setMax((Double)document.get(MAX));
        attribute.setDefaultValue(getStringOrNull(document, DEFAULT));
        attribute.setDelimiter(getStringOrNull(document, DELIMITER));
        attribute.setStep(getStringOrNull(document, STEP));
        attribute.setRequired((Boolean)document.get(REQUIRED));
        attribute.setRegex(getStringOrNull(document, REGEX));
        attribute.setEditableInTable((Boolean)document.get(EDITABLE_IN_TABLE));
        attribute.setDateFormat(getStringOrNull(document, DATE_FORMAT));
        attribute.setEntity(getStringOrNull(document, ENTITY));

        return attribute;
    }


    @Override
    public Document getDocument(Identifiable object)
    {
        Attribute attribute = (Attribute)object;

        Document document = new Document()
                .append(NAME, attribute.getName())
                .append(TITLE, attribute.getTitle())
                .append(METHOD, attribute.getMethod())
                .append(TYPE, attribute.getType())
                .append(MIN_WIDTH, attribute.getMinWidth())
                .append(MAX_WIDTH, attribute.getMaxWidth())
                .append(MIN_HEIGHT, attribute.getMinHeight())
                .append(MAX_HEIGHT, attribute.getMaxHeight())
                .append(IMAGES_SIZE, attribute.getImagesSize())
                .append(LINES_COUNT, attribute.getLinesCount())
                .append(ALIGNMENT, attribute.getAlignment())
                .append(DEFAULT, attribute.getDefaultValue())
                .append(STEP, attribute.getStep())
                .append(MAX, attribute.getMax())
                .append(MIN, attribute.getMin())
                .append(REQUIRED, attribute.getRequired())
                .append(REGEX, attribute.getRegex())
                .append(DELIMITER, attribute.getDelimiter())
                .append(EDITABLE_IN_TABLE, attribute.getEditableInTable())
                .append(DATE_FORMAT, attribute.getDateFormat())
                .append(SELECT_OPTIONS, attribute.getSelectOptions())
                .append(ENTITY, attribute.getEntity());

        if (!Strings.isNullOrEmpty(attribute.getId()))
            document.append(MONGO_ID, new ObjectId(attribute.getId()));

        return document;
    }
}
