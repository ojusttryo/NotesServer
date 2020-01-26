package ru.justtry.mappers;

import static ru.justtry.shared.Constants.MONGO_ID;
import static ru.justtry.shared.EntityConstants.ATTRIBUTES;
import static ru.justtry.shared.EntityConstants.COLLECTION;
import static ru.justtry.shared.EntityConstants.TITLE;

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
        entity.setCollection(document.get(COLLECTION).toString());
        entity.setTitle(document.get(TITLE).toString());
        entity.setAttributes((List<String>)document.get(ATTRIBUTES));

        return entity;
    }

    @Override
    public Document getDocument(Object object)
    {
        Entity entity = (Entity)object;

        Document document = new Document()
                .append(COLLECTION, entity.getCollection())
                .append(TITLE, entity.getTitle())
                .append(ATTRIBUTES, entity.getAttributes());

        if (!Strings.isNullOrEmpty(entity.getId()))
            document.append(MONGO_ID, new ObjectId(entity.getId()));

        return document;
    }
}
