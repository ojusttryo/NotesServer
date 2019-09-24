package ru.justtry.attributes;

import com.google.common.base.Strings;
import com.mongodb.DBObject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import ru.justtry.database.Database;
import ru.justtry.shared.Mapper;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static ru.justtry.attributes.AttributeConstants.*;
import static ru.justtry.attributes.EntityConstants.ATTRIBUTES;
import static ru.justtry.shared.Constants.MONGO_ID;
import static ru.justtry.shared.Constants.NAME;

@Component
public class EntityMapper extends Mapper
{
    @Override
    public Object getObject(Document document)
    {
        Entity entity = new Entity();
        entity.setId(document.get(MONGO_ID).toString());
        entity.setName(document.get(NAME).toString());
        entity.setAttributes((List<String>)document.get(ATTRIBUTES));

//        for (String attribute : attributes)
//            entity.getAttributes().add(attribute);

        return entity;
    }


    @Override
    public Document getDocument(String id, Map<String, Object> values)
    {
        Document document = new Document();

        if (!Strings.isNullOrEmpty(id))
            document.append(MONGO_ID, new ObjectId(id));

        document.append(NAME, values.get(NAME));
        document.append(ATTRIBUTES, values.get(ATTRIBUTES));    // expected List<String>

        return document;
    }

    @Override
    public Document getDocument(Object object)
    {
        Entity entity = (Entity)object;

        Document document = new Document()
                .append(NAME, entity.getName())
                .append(ATTRIBUTES, entity.getAttributes());

        if (!Strings.isNullOrEmpty(entity.getId()))
            document.append(MONGO_ID, new ObjectId(entity.getId()));

        return document;
    }
}
