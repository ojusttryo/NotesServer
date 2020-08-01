package ru.justtry.mappers;

import static ru.justtry.shared.AttributeConstants.*;
import static ru.justtry.shared.Constants.MONGO_ID;
import static ru.justtry.shared.Constants.NAME;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import ru.justtry.metainfo.Attribute;

@Component
public class AttributeMapper extends Mapper
{
    @Override
    public Object getObject(Document document)
    {
        Attribute attribute = new Attribute();

        attribute.setId(document.get(MONGO_ID).toString());
        attribute.setName(document.get(NAME).toString());
        attribute.setTitle(document.get(TITLE).toString());
        attribute.setAlignment(document.get(ALIGNMENT).toString());
        attribute.setLinesCount((Integer)document.get(LINES_COUNT));
        attribute.setType(document.get(TYPE).toString());
        attribute.setSelectOptions((List<String>)document.get(SELECT_OPTIONS));
        attribute.setVisible((Boolean)document.get(VISIBLE));
        attribute.setMethod(getStringOrNull(document, METHOD));
        attribute.setMaxWidth(getStringOrNull(document, MAX_WIDTH));
        attribute.setMinWidth(getStringOrNull(document, MIN_WIDTH));
        attribute.setMin(getStringOrNull(document, MIN));
        attribute.setMax(getStringOrNull(document, MAX));
        attribute.setDefaultValue(getStringOrNull(document, DEFAULT));
        attribute.setStep(getStringOrNull(document, STEP));
        attribute.setRequired((Boolean)document.get(REQUIRED));
        attribute.setRegex(getStringOrNull(document, REGEX));

        return attribute;
    }


    @Override
    public Document getDocument(Object object)
    {
        Attribute attribute = (Attribute)object;

        Document document = new Document()
                .append(NAME, attribute.getName())
                .append(TITLE, attribute.getTitle())
                .append(METHOD, attribute.getMethod())
                .append(VISIBLE, attribute.getVisible())
                .append(TYPE, attribute.getType())
                .append(MIN_WIDTH, attribute.getMinWidth())
                .append(MAX_WIDTH, attribute.getMaxWidth())
                .append(LINES_COUNT, attribute.getLinesCount())
                .append(ALIGNMENT, attribute.getAlignment())
                .append(DEFAULT, attribute.getDefaultValue())
                .append(STEP, attribute.getStep())
                .append(MAX, attribute.getMax())
                .append(MIN, attribute.getMin())
                .append(REQUIRED, attribute.getRequired())
                .append(REGEX, attribute.getRegex());

        if (!Strings.isNullOrEmpty(attribute.getId()))
            document.append(MONGO_ID, new ObjectId(attribute.getId()));

        if (attribute.getSelectOptions() != null && !attribute.getSelectOptions().isEmpty())
            document.append(SELECT_OPTIONS, attribute.getSelectOptions());

        return document;
    }
}
