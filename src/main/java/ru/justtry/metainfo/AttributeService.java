package ru.justtry.metainfo;

import static ru.justtry.shared.AttributeConstants.ATTRIBUTES_COLLECTION;
import static ru.justtry.shared.AttributeConstants.NAME;

import java.util.Arrays;
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
        List<Document> documents = database.getDocuments(ATTRIBUTES_COLLECTION, NAME);
        Identifiable[] attributes = attributeMapper.getObjects(documents);
        return toAttributesArray(attributes);
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
