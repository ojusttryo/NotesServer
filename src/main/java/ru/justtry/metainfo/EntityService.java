package ru.justtry.metainfo;

import static ru.justtry.shared.EntityConstants.ENTITIES_COLLECTION;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.justtry.database.Database;
import ru.justtry.mappers.EntityMapper;

@Component
public class EntityService
{
    @Autowired
    private EntityMapper entityMapper;
    @Autowired
    protected Database database;

    public Entity getByName(String name)
    {
        Document doc = database.getEntity(name);
        Entity entity = (Entity)entityMapper.getObject(doc);
        return entity;
    }


    public Entity getById(String id)
    {
        Document doc = database.getDocument(ENTITIES_COLLECTION, id);
        Entity entity = (Entity)entityMapper.getObject(doc);
        return entity;
    }


    public void update(Entity entity)
    {
        Document document = entityMapper.getDocument(entity);
        database.updateDocument(ENTITIES_COLLECTION, document);
    }
}
