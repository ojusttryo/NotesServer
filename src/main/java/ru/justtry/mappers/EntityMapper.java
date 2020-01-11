package ru.justtry.mappers;

import static ru.justtry.shared.Constants.MONGO_ID;
import static ru.justtry.shared.Constants.NAME;
import static ru.justtry.shared.EntityConstants.ATTRIBUTES;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import ru.justtry.metainfo.Entity;

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

        return entity;
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
