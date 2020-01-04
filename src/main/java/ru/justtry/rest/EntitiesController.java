package ru.justtry.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.justtry.metainfo.Entity;
import ru.justtry.mappers.EntityMapper;
import ru.justtry.validation.EntityValidator;
import ru.justtry.mappers.Mapper;
import ru.justtry.validation.Validator;

import javax.inject.Inject;

import static ru.justtry.shared.EntityConstants.ENTITIES_COLLECTION;

@RestController
@RequestMapping("/rest/entities")
public class EntitiesController extends MetainfoController
{
    @Inject
    private EntityMapper entityMapper;
    @Inject
    private EntityValidator entityValidator;


    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public String save(@RequestBody Entity entity)
//            @RequestParam(value = NAME) String name,
//            @RequestParam(value = ATTRIBUTES) String metainfo[])
    {
//        Entity entity = new Entity();
//
//        entity.setName(name);
//        entity.setAttributes(Arrays.asList(metainfo));
//
//        return database.saveEntity(entity);

//        Map<String, Object> values = new HashMap<>();
//        values.put(NAME, name);
//        values.put(ATTRIBUTES, Arrays.asList(metainfo));

        return database.saveDocument(ENTITIES_COLLECTION, entityValidator, entityMapper, entity);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public void update(@RequestBody Entity entity)
//            @RequestParam(value = MONGO_ID) String id,
//            @RequestParam(value = NAME) String name,
//            @RequestParam(value = ATTRIBUTES) String metainfo[])
    {
//        Map<String, Object> values = new HashMap<>();
//        values.put(NAME, name);
//        values.put(ATTRIBUTES, Arrays.asList(metainfo));

        //Document document = entityMapper.getDocument(id, values);

//        entity.setId(id);
//        entity.setName(name);
//        entity.setAttributes(Arrays.asList(metainfo));

        database.updateDocument(ENTITIES_COLLECTION, entityValidator, entityMapper, entity);
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
