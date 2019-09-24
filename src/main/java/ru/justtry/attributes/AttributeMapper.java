package ru.justtry.attributes;

import com.google.common.base.Strings;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import ru.justtry.shared.Mapper;

import java.util.Map;

import static ru.justtry.attributes.AttributeConstants.*;
import static ru.justtry.attributes.AttributeConstants.MIN_WIDTH;
import static ru.justtry.shared.Constants.MONGO_ID;
import static ru.justtry.shared.Constants.NAME;

@Component
public class AttributeMapper implements Mapper
{
    @Override
    public Object getObject(Document document)
    {
        Attribute attribute = new Attribute();

        attribute.setId(document.get(MONGO_ID).toString());
        attribute.setName(document.get(NAME).toString());
        attribute.setAlignment(document.get(ALIGNMENT).toString());
        attribute.setLinesCount((Integer)document.get(LINES_COUNT));
        attribute.setType(document.get(TYPE).toString());
        attribute.setVisible((Boolean)document.get(VISIBLE));
        attribute.setMethod(document.get(METHOD).toString());
        attribute.setMaxWidth(document.get(MAX_WIDTH).toString());
        attribute.setMinWidth(document.get(MIN_WIDTH).toString());
        attribute.setMinValue(getStringOrNull(document, MIN_VALUE));
        attribute.setMaxValue(getStringOrNull(document, MAX_VALUE));
        attribute.setDefaultValue(getStringOrNull(document, DEFAULT));
        attribute.setRequired((Boolean)document.get(REQUIRED));
        attribute.setRegex(getStringOrNull(document, REGEX));

        return attribute;
    }


    private String getStringOrNull(Document document, String key)
    {
        return (document.containsKey(key) && document.get(key) != null) ? document.get(key).toString() : null;
    }


    @Override
    public Document getDocument(String id, Map<String, Object> values)
    {
        Document document = new Document();

        if (!Strings.isNullOrEmpty(id))
            document.append(MONGO_ID, new ObjectId(id));

        for (String key : values.keySet())
            document.append(key, values.get(key));

        return document;
    }

    @Override
    public Document getDocument(Object object)
    {
        Attribute attribute = (Attribute)object;

        Document document = new Document()
                .append(NAME, attribute.getName())
                .append(METHOD, attribute.getMethod())
                .append(VISIBLE, attribute.getVisible())
                .append(TYPE, attribute.getType())
                .append(MIN_WIDTH, attribute.getMinWidth())
                .append(MAX_WIDTH, attribute.getMaxWidth())
                .append(LINES_COUNT, attribute.getLinesCount())
                .append(ALIGNMENT, attribute.getAlignment())
                .append(DEFAULT, attribute.getDefaultValue())
                .append(MAX_VALUE, attribute.getMaxValue())
                .append(MIN_VALUE, attribute.getMinValue())
                .append(REQUIRED, attribute.getRequired())
                .append(REGEX, attribute.getRegex());

        if (!Strings.isNullOrEmpty(attribute.getId()))
            document.append(MONGO_ID, new ObjectId(attribute.getId()));

        return document;
    }


}
