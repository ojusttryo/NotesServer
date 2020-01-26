package ru.justtry.rest;

import static ru.justtry.shared.EntityConstants.ENTITIES_COLLECTION;

import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ru.justtry.mappers.EntityMapper;
import ru.justtry.mappers.Mapper;
import ru.justtry.metainfo.Entity;
import ru.justtry.validation.EntityValidator;
import ru.justtry.validation.Validator;

@RestController
@RequestMapping("/rest/entities")
public class EntitiesController extends MetaInfoController
{
    @Inject
    private EntityMapper entityMapper;
    @Inject
    private EntityValidator entityValidator;


    @PostMapping(consumes = "application/json;charset=UTF-8", produces = "text/plain;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String save(@RequestBody Entity entity)
//            @RequestParam(value = NAME) String name,
//            @RequestParam(value = ATTRIBUTES) String metainfo[])
    {
//        Entity entity = new Entity();
//
//        entity.setCollection(name);
//        entity.setAttributes(Arrays.asList(metainfo));
//
//        return database.saveEntity(entity);

//        Map<String, Object> values = new HashMap<>();
//        values.put(NAME, name);
//        values.put(ATTRIBUTES, Arrays.asList(metainfo));
        return database.saveDocument(ENTITIES_COLLECTION, entityValidator, entityMapper, entity);
    }

    @PutMapping(consumes = "application/json;charset=UTF-8", produces = "text/plain;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    public String update(@RequestBody Entity entity)
//            @RequestParam(value = MONGO_ID) String id,
//            @RequestParam(value = NAME) String name,
//            @RequestParam(value = ATTRIBUTES) String metainfo[])
    {
//        Map<String, Object> values = new HashMap<>();
//        values.put(NAME, name);
//        values.put(ATTRIBUTES, Arrays.asList(metainfo));

        //Document document = entityMapper.getDocument(id, values);

//        entity.setId(id);
//        entity.setCollection(name);
//        entity.setAttributes(Arrays.asList(metainfo));

        database.updateDocument(ENTITIES_COLLECTION, entityValidator, entityMapper, entity);
        return entity.getId();
//        database.updateEntity(document);
    }

//    @GetMapping("/{_id}")
//    @ResponseBody
//    public Object get(@PathVariable(value = MONGO_ID) String id)
//    {
//        return database.getObject(ENTITIES_COLLECTION, entityMapper, id);
//    }

    @Override
    public Mapper getMapper()
    {
        return entityMapper;
    }

    @Override
    public Validator getValidator()
    {
        return entityValidator;
    }

    @Override
    public String getCollectionName()
    {
        return ENTITIES_COLLECTION;
    }
}
