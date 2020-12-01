package ru.justtry.metainfo;

import static ru.justtry.shared.AttributeConstants.ATTRIBUTES_COLLECTION;
import static ru.justtry.shared.AttributeConstants.NAME;
import static ru.justtry.shared.AttributeConstants.TITLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.justtry.database.Database;
import ru.justtry.mappers.AttributeMapper;
import ru.justtry.shared.Identifiable;

@Component
public class AttributeService
{
    @Autowired
    private AttributeMapper attributeMapper;
    @Autowired
    protected Database database;
    @Autowired
    private EntityService entityService;


    public Attribute getById(String id)
    {
        Document document = database.getDocument(ATTRIBUTES_COLLECTION, id);
        Attribute attribute = (Attribute)attributeMapper.getObject(document);
        return attribute;
    }


    public Attribute getByName(String name)
    {
        Document document = database.getAttribute(name);
        if (document == null)
            return null;
        Attribute attribute = (Attribute)attributeMapper.getObject(document);
        return attribute;
    }


    public String getId(String name)
    {
        Attribute attribute = getByName(name);
        return (attribute == null) ? null : attribute.getId();
    }


    public Attribute[] get(List<String> names)
    {
        List<Document> documents = database.getDocuments(ATTRIBUTES_COLLECTION, names, NAME);
        Identifiable[] attributes = attributeMapper.getObjects(documents);
        return toAttributesArray(attributes);
    }


    public Attribute[] get(String entityName)
    {
        Entity entity = entityService.getByName(entityName);
        Identifiable[] attributes = get(entity.getAttributes());
        return toAttributesArray(attributes);
    }


    public Attribute[] getAll()
    {
        List<Document> documents = database.getDocuments(ATTRIBUTES_COLLECTION, TITLE);
        Identifiable[] attributes = attributeMapper.getObjects(documents);
        return toAttributesArray(attributes);
    }


    public Attribute[] getAllWithUsage()
    {
        Attribute[] attributes = getAll();
        Map<String, Attribute> attributeMap = Arrays.stream(attributes)
                .collect(Collectors.toMap(Attribute::getName, x -> x));

        Entity[] entities = entityService.getAll();
        for (Entity entity : entities)
        {
            for (String attribute : entity.getAttributes())
                attributeMap.get(attribute).getUsage().add(entity.getName());
        }
        return attributes;
    }


    public Attribute[] getAvailable(Entity entity)
    {
        Entity[] entities = entityService.getAll();
        Attribute[] attributes = getAll();
        Map<String, Attribute> attrMap = Arrays.stream(attributes).collect(Collectors.toMap(x -> x.getName(), x -> x));

        List<Attribute> available = new ArrayList<>();
        Map<String, Integer> usage = new HashMap<>();
        for (Attribute attribute : attributes)
        {
            if (attribute.isShared())
                available.add(attribute);
            else
                usage.put(attribute.getName(), 0);
        }

        for (Entity e : entities)
        {
            for (String entityAttr : e.getAttributes())
            {
                if (usage.containsKey(entityAttr))
                    usage.put(entityAttr, usage.get(entityAttr) + 1);
            }
        }

        for (Map.Entry<String, Integer> entry : usage.entrySet())
        {
            if (entry.getValue() == 0 || (entity != null && entity.getAttributes().contains(entry.getKey())))
                available.add(attrMap.get(entry.getKey()));
        }

        available.sort(Comparator.comparing(Attribute::getName));

        return available.toArray(Attribute[]::new);
    }


    public void save(Attribute attribute)
    {
        Document document = attributeMapper.getDocument(attribute);
        String id = database.saveDocument(ATTRIBUTES_COLLECTION, document);
        attribute.setId(id);
    }


    public void update(Attribute attribute)
    {
        Document document = attributeMapper.getDocument(attribute);
        database.updateDocument(ATTRIBUTES_COLLECTION, document);
    }


    public Map<String, Attribute> getAttributesAsMap(String entityName)
    {
        Attribute[] objects = get(entityName);
        Map<String, Attribute> attributes = Arrays.stream(objects)
                .collect(Collectors.toMap(attr -> attr.getName(), attr -> attr));
        return attributes;
    }


    private Attribute[] toAttributesArray(Identifiable[] attributes)
    {
        return Arrays.stream(attributes).map(x -> (Attribute)x).toArray(Attribute[]::new);
    }
}
