package ru.justtry.shared;

import static ru.justtry.shared.AttributeConstants.ATTRIBUTES_COLLECTION;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.justtry.database.Database;
import ru.justtry.mappers.AttributeMapper;
import ru.justtry.mappers.EntityMapper;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.Entity;

@Component
public class Utils
{
    @Inject
    private Database database;
    @Inject
    private AttributeMapper attributeMapper;
    @Autowired
    private EntityMapper entityMapper;


    public Map<String, Attribute> getAttributes(String entityName)
    {
        Entity entity = (Entity)entityMapper.getObject(database.getEntity(entityName));
        List<Document> documents = database.getDocuments(ATTRIBUTES_COLLECTION, entity.getAttributes());
        Identifiable[] objects = attributeMapper.getObjects(documents);
        return Arrays.stream(objects).collect(Collectors.toMap(attr -> ((Attribute)attr).getName(), attr -> (Attribute)attr));
    }
}
