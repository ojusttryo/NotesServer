package ru.justtry.attributes;

import org.bson.Document;
import org.springframework.stereotype.Component;
import ru.justtry.shared.Mapper;

import static ru.justtry.attributes.AttributeConstants.*;
import static ru.justtry.attributes.AttributeConstants.MIN_WIDTH;
import static ru.justtry.shared.Constants.NAME;

@Component
public class AttributeMapper implements Mapper
{
    @Override
    public Object get(Document document)
    {
        Attribute attribute = new Attribute();

        attribute.setName(document.get(NAME).toString());
        attribute.setAlignment(document.get(ALIGNMENT).toString());
        attribute.setLinesCount((Integer)document.get(LINES_COUNT));
        attribute.setType(document.get(TYPE).toString());
        attribute.setVisible((Boolean)document.get(VISIBLE));
        attribute.setMethod(document.get(METHOD).toString());
        attribute.setMaxWidth(document.get(MAX_WIDTH).toString());
        attribute.setMinWidth(document.get(MIN_WIDTH).toString());

        return attribute;
    }
}
