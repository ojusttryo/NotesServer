package ru.justtry.mappers;

import static ru.justtry.shared.Constants.MONGO_ID;
import static ru.justtry.shared.EntityConstants.ATTRIBUTES;
import static ru.justtry.shared.EntityConstants.COLLECTION;
import static ru.justtry.shared.EntityConstants.TITLE;
import static ru.justtry.shared.EntityConstants.VISIBLE;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import ru.justtry.metainfo.Entity;
import ru.justtry.shared.Identifiable;

@Component
public class EntityMapper extends Mapper
{
    @Override
    public Identifiable getObject(Document document)
    {
        Entity entity = new Entity();
        entity.setId(document.get(MONGO_ID).toString());
        entity.setCollection(document.get(COLLECTION).toString());
        entity.setTitle(document.get(TITLE).toString());
        entity.setVisible((boolean)document.get(VISIBLE));
        entity.setAttributes((List<String>)document.get(ATTRIBUTES));

        return entity;
    }


    @Override
    public Document getDocument(Identifiable object)
    {
        Entity entity = (Entity)object;

        Document document = new Document()
                .append(COLLECTION, entity.getCollection())
                .append(TITLE, entity.getTitle())
                .append(VISIBLE, entity.isVisible())
                .append(ATTRIBUTES, entity.getAttributes());

        if (!Strings.isNullOrEmpty(entity.getId()))
            document.append(MONGO_ID, new ObjectId(entity.getId()));

        return document;
    }
}
