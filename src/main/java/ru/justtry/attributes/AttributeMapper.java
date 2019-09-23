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

        return attribute;
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
}
