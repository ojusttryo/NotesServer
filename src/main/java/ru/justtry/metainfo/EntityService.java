package ru.justtry.metainfo;

import static ru.justtry.shared.EntityConstants.ATTRIBUTES;
import static ru.justtry.shared.EntityConstants.ENTITIES_COLLECTION;
import static ru.justtry.shared.EntityConstants.TITLE;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.justtry.database.Database;
import ru.justtry.mappers.EntityMapper;
import ru.justtry.shared.Identifiable;

@Service
@RequiredArgsConstructor
public class EntityService
{
    private final EntityMapper entityMapper;
    private final Database database;


    public Entity[] getAll()
    {
        List<Document> docs = database.getDocuments(ENTITIES_COLLECTION, TITLE);
        Identifiable[] entities = entityMapper.getObjects(docs);
        return toEntitiesArray(entities);
    }


    public Entity[] getByAttribute(String attributeName)
    {
        // Not the best solution in terms of performance but it's a rare operation and there are very few entities
        List<Document> docs = database.getDocuments(ENTITIES_COLLECTION, TITLE).stream()
                .filter(x ->
                {
                    Object a = x.get(ATTRIBUTES);
                    List<String> attributes = (List<String>)x.get(ATTRIBUTES);
                    return attributes.contains(attributeName);
                })
                .collect(Collectors.toList());
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
        return (Entity)entityMapper.getObject(doc);
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
