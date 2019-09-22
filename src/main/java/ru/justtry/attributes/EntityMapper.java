package ru.justtry.attributes;

import com.mongodb.DBObject;
import org.bson.Document;
import org.springframework.stereotype.Component;
import ru.justtry.database.Database;
import ru.justtry.shared.Mapper;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static ru.justtry.attributes.AttributeConstants.*;
import static ru.justtry.attributes.EntityConstants.ATTRIBUTES;
import static ru.justtry.shared.Constants.NAME;

@Component
public class EntityMapper implements Mapper
{
    @Override
    public Object get(Document document)
    {
        Entity entity = new Entity();
        entity.setName(document.get(NAME).toString());
        entity.setAttributes((List<String>)document.get(ATTRIBUTES));

//        for (String attribute : attributes)
//            entity.getAttributes().add(attribute);

        return entity;
    }
}
