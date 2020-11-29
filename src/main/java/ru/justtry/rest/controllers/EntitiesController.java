package ru.justtry.rest.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static ru.justtry.shared.EntityConstants.ENTITIES_COLLECTION;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ru.justtry.database.Database;
import ru.justtry.mappers.EntityMapper;
import ru.justtry.metainfo.Entity;
import ru.justtry.metainfo.EntityService;
import ru.justtry.postprocessing.DeleteEntityPostprocessor;
import ru.justtry.postprocessing.SaveEntityPostprocessor;
import ru.justtry.shared.Identifiable;
import ru.justtry.validation.EntityValidator;

@RestController
@CrossOrigin(maxAge = 3600)
@RequestMapping("/rest/entities")
public class EntitiesController
{
    final static Logger logger = LogManager.getLogger(EntitiesController.class);

    @Autowired
    private Database database;
    @Autowired
    private EntityMapper entityMapper;
    @Autowired
    private EntityValidator entityValidator;
    @Autowired
    private SaveEntityPostprocessor savePostprocessor;
    @Autowired
    private DeleteEntityPostprocessor deletePostprocessor;
    @Autowired
    private EntityService entityService;


    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<Object> save(@RequestBody Entity entity)
    {
        entityValidator.validate(entity, ENTITIES_COLLECTION);
        Document document = entityMapper.getDocument(entity);
        String id = database.saveDocument(ENTITIES_COLLECTION, document);
        entity.setId(id);
        savePostprocessor.process(entity);
        database.saveLog(ENTITIES_COLLECTION, "CREATE", id, null, entity.toString());
        return new ResponseEntity<>(id, new HttpHeaders(), HttpStatus.OK);
    }

    @PutMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> update(@RequestBody Entity entity)
    {
        Identifiable before = entityService.getByName(entity.getName());
        entity.setId(before.getId());
        entityValidator.validate(entity, ENTITIES_COLLECTION);
        entityService.update(entity);
        savePostprocessor.process(entity);
        database.saveLog(ENTITIES_COLLECTION, "UPDATE", entity.getId(), before.toString(), entity.toString());
        return new ResponseEntity<>(entity.getId(), new HttpHeaders(), HttpStatus.OK);
    }


    @GetMapping(path = "/search", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Object get(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String attribute)
    {
        if (id != null)
            return entityService.getById(id);
        else if (name != null)
            return entityService.getByName(name);
        else if (attribute != null)
            return entityService.getByAttribute(attribute);
        else
            return entityService.getAll();
    }


    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Entity[] get()
    {
        return entityService.getAll();
    }


    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable String name)
    {
        Entity before = entityService.getByName(name);
        database.deleteDocument(ENTITIES_COLLECTION, before.getId());
        deletePostprocessor.process(before);
        database.saveLog(ENTITIES_COLLECTION, "DELETE", name, before.toString(), null);
    }
}
