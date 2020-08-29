package ru.justtry.metainfo;

import static ru.justtry.shared.AttributeConstants.ATTRIBUTES_COLLECTION;

import java.util.ArrayList;
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
        Attribute attribute = (Attribute)attributeMapper.getObject(document);
        return attribute;
    }


    public Identifiable[] get(List<String> ids)
    {
        List<Document> documents = database.getDocuments(ATTRIBUTES_COLLECTION, ids);
        Identifiable[] attributes = attributeMapper.getObjects(documents);
        return attributes;
    }


    public Identifiable[] getAll()
    {
        List<Document> documents = database.getDocuments(ATTRIBUTES_COLLECTION, new ArrayList<>());
        Identifiable[] attributes = attributeMapper.getObjects(documents);
        return attributes;
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
        Entity entity = entityService.getByName(entityName);
        Identifiable[] objects = get(entity.getAttributes());
        Map<String, Attribute> attributes = Arrays.stream(objects)
                .collect(Collectors.toMap(attr ->
                        ((Attribute)attr).getName(),
                        attr -> (Attribute)attr));
        return attributes;
    }
}
