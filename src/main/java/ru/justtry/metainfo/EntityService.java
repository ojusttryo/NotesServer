package ru.justtry.metainfo;

import static ru.justtry.shared.EntityConstants.ENTITIES_COLLECTION;
import static ru.justtry.shared.EntityConstants.NAME;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.justtry.database.Database;
import ru.justtry.mappers.EntityMapper;
import ru.justtry.shared.Identifiable;

@Component
public class EntityService
{
    @Autowired
    private EntityMapper entityMapper;
    @Autowired
    protected Database database;


    public Entity[] getAll()
    {
        List<Document> docs = database.getDocuments(ENTITIES_COLLECTION, NAME);
        Identifiable[] entities = entityMapper.getObjects(docs);
        return toEntitiesArray(entities);
    }


    public Entity getByName(String name)
    {
        Document doc = database.getEntity(name);
        if (doc == null)
            return null;

        Entity entity = (Entity)entityMapper.getObject(doc);
        return entity;
    }


    public String getId(String name)
    {
        Entity entity = getByName(name);
        return (entity == null) ? null : entity.getName();
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


    private Entity[] toEntitiesArray(Identifiable[] entities)
    {
        return Arrays.stream(entities).map(x -> (Entity)x).toArray(Entity[]::new);
    }
}
