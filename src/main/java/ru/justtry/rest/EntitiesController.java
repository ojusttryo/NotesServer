package ru.justtry.rest;

import static ru.justtry.shared.Constants.ID;
import static ru.justtry.shared.EntityConstants.ENTITIES_COLLECTION;
import static ru.justtry.shared.EntityConstants.NAME;

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
import ru.justtry.shared.Utils;
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
    @Autowired
    private Utils utils;


    @PostMapping(consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<Object> save(@RequestBody Entity entity)
    {
        HttpHeaders headers = new HttpHeaders();
        try
        {
            entityValidator.validate(entity, ENTITIES_COLLECTION);
            Document document = entityMapper.getDocument(entity);
            String id = database.saveDocument(ENTITIES_COLLECTION, document);
            entity.setId(id);
            savePostprocessor.process(entity);
            database.saveLog(ENTITIES_COLLECTION, "CREATE", id, null, entity.toString());
            return new ResponseEntity<>(id, headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            logger.error(e);
            return utils.getResponseForError(headers, e);
        }
    }

    @PutMapping(consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> update(@RequestBody Entity entity)
    {
        HttpHeaders headers = new HttpHeaders();
        try
        {
            entityValidator.validate(entity, ENTITIES_COLLECTION);
            Identifiable before = entityService.getById(entity.getId());
            entityService.update(entity);
            savePostprocessor.process(entity);
            database.saveLog(ENTITIES_COLLECTION, "UPDATE", entity.getId(), before.toString(), entity.toString());
            return new ResponseEntity<>(entity.getId(), headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            logger.error(e);
            return utils.getResponseForError(headers, e);
        }
    }


    @GetMapping(path = "/search", produces = "application/json;charset=UTF-8")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Object get(
            @RequestParam(value = ID, required = false) String id,
            @RequestParam(value = NAME, required = false) String name)
    {
        if (id != null)
            return entityService.getById(id);
        else if (name != null)
            return entityService.getByName(name);
        else
            return entityService.getAll();
    }


    @GetMapping(produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Entity[] get()
    {
        return entityService.getAll();
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable(value = ID) String id)
    {
        Entity before = entityService.getById(id);
        database.deleteDocument(ENTITIES_COLLECTION, id);
        deletePostprocessor.process(before);
        database.saveLog(ENTITIES_COLLECTION, "DELETE", id, before.toString(), null);
    }
}
