package ru.justtry.rest;

import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.justtry.attributes.Entity;
import ru.justtry.attributes.EntityMapper;
import ru.justtry.attributes.EntityValidator;
import ru.justtry.database.Database;
import ru.justtry.shared.Controller;
import ru.justtry.shared.Mapper;
import ru.justtry.shared.Validator;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static ru.justtry.attributes.EntityConstants.ATTRIBUTES;
import static ru.justtry.attributes.EntityConstants.ENTITIES_COLLECTION;
import static ru.justtry.shared.Constants.*;

@RestController
@RequestMapping("/rest/entities")
public class EntityController extends Controller
{
    @Inject
    private EntityMapper entityMapper;
    @Inject
    private EntityValidator entityValidator;


    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public String save(
            @RequestParam(value = NAME) String name,
            @RequestParam(value = ATTRIBUTES) String attributes[])
    {
//        Entity entity = new Entity();
//
//        entity.setName(name);
//        entity.setAttributes(Arrays.asList(attributes));
//
//        return database.saveEntity(entity);

        Map<String, Object> values = new HashMap<>();
        values.put(NAME, name);
        values.put(ATTRIBUTES, Arrays.asList(attributes));

        return database.saveDocument(ENTITIES_COLLECTION, entityValidator, entityMapper, values);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public void update(
            @RequestParam(value = MONGO_ID) String id,
            @RequestParam(value = NAME) String name,
            @RequestParam(value = ATTRIBUTES) String attributes[])
    {
        Map<String, Object> values = new HashMap<>();
        values.put(NAME, name);
        values.put(ATTRIBUTES, Arrays.asList(attributes));

        //Document document = entityMapper.getDocument(id, values);

//        entity.setId(id);
//        entity.setName(name);
//        entity.setAttributes(Arrays.asList(attributes));

        database.updateDocument(ENTITIES_COLLECTION, entityValidator, entityMapper, id, values);
//        database.updateEntity(document);
    }

//    @GetMapping("/{_id}")
//    @ResponseBody
//    public Object get(@PathVariable(value = MONGO_ID) String id)
//    {
//        return database.getObject(ENTITIES_COLLECTION, entityMapper, id);
//    }

    @Override
    protected Mapper getMapper()
    {
        return entityMapper;
    }

    @Override
    protected Validator getValidator()
    {
        return entityValidator;
    }

    @Override
    protected String getCollectionName()
    {
        return ENTITIES_COLLECTION;
    }
}
