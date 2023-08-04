package ru.justtry.rest;

import static ru.justtry.shared.AttributeConstants.ATTRIBUTES_COLLECTION;
import static ru.justtry.shared.AttributeConstants.NAME;
import static ru.justtry.shared.AttributeConstants.SHARED;
import static ru.justtry.shared.RestConstants.REST_ATTRIBUTES;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.AttributeService;
import ru.justtry.metainfo.Entity;
import ru.justtry.metainfo.EntityService;
import ru.justtry.postprocessing.DeleteAttributePostprocessor;
import ru.justtry.postprocessing.SaveAttributePostprocessor;
import ru.justtry.shared.Utils;
import ru.justtry.validation.AttributeValidator;


@RestController
@CrossOrigin(maxAge = 3600)
@RequestMapping(REST_ATTRIBUTES)
public class AttributesController
{
    final static Logger logger = LogManager.getLogger(AttributesController.class);

    @Autowired
    private Database database;
    @Autowired
    private AttributeValidator attributeValidator;
    @Autowired
    private SaveAttributePostprocessor savePostprocessor;
    @Autowired
    private DeleteAttributePostprocessor deletePostprocessor;
    @Autowired
    private AttributeService attributeService;
    @Autowired
    private EntityService entityService;
    @Autowired
    private Utils utils;


    @PostMapping(consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<Object> save(@RequestBody Attribute attribute)
    {
        HttpHeaders headers = new HttpHeaders();
        try
        {
            attributeValidator.validate(attribute, ATTRIBUTES_COLLECTION);
            attributeService.save(attribute);
            savePostprocessor.process(attribute);
            database.saveLog(ATTRIBUTES_COLLECTION, "CREATE", attribute.getId(), null, attribute.toString());
            return new ResponseEntity<>(attribute.getId(), headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            // TODO roll back actions (delete note, etc)
            logger.error(e);
            return utils.getResponseForError(headers, e);
        }
    }

    @PutMapping(consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> update(@RequestBody Attribute attribute)
    {
        HttpHeaders headers = new HttpHeaders();
        try
        {
            attribute.setId(attributeService.getId(attribute.getName()));
            attributeValidator.validate(attribute, ATTRIBUTES_COLLECTION);
            Attribute before = attributeService.getByName(attribute.getName());
            attributeService.update(attribute);
            savePostprocessor.process(attribute);
            database.saveLog(ATTRIBUTES_COLLECTION, "UPDATE", attribute.getId(), before.toString(), attribute.toString());
            return new ResponseEntity<>(attribute.getId(), headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            logger.error(e);
            return utils.getResponseForError(headers, e);
        }
    }


    @GetMapping(path = "/search", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object get(
            @RequestParam(value = "entityName", required = false) String entityName,
            @RequestParam(value = NAME, required = false) String name,
            @RequestParam(value = "visible", required = false) Boolean visible,
            @RequestParam(value = SHARED, required = false) Boolean shared)
    {
        // all by shared
        if (shared != null && shared)
        {
            if (entityName != null)
            {
                Entity entity = entityService.getByName(entityName);
                Attribute[] attributes = attributeService.getAvailable(entity);
                return attributes;
            }
            else
            {
                return attributeService.getAvailable(null);
            }
        }
        // all by visible
        if (entityName != null)
        {
            boolean isVisible = (visible != null && visible);
            Entity entity = entityService.getByName(entityName);
            return isVisible ? attributeService.get(entity.getVisibleAttributes())
                    : attributeService.get(entity.getAttributes());
        }
        // single by name
        else if (name != null)
        {
            return attributeService.getByName(name);
        }
        // all
        else
        {
            return attributeService.getAll();
        }
    }


    @GetMapping(path = "/compared/{entityName}", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object get(@PathVariable(value = "entityName") String entityName)
    {
        Entity entity = entityService.getByName(entityName);
        return attributeService.get(entity.getComparedAttributes());
    }


    @GetMapping(produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Attribute[] get()
    {
        return attributeService.getAll();
    }


    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable(value = NAME) String name)
    {
        Attribute before = attributeService.getByName(name);
        database.deleteDocument(ATTRIBUTES_COLLECTION, before.getId());
        deletePostprocessor.process(before);
        database.saveLog(ATTRIBUTES_COLLECTION, "DELETE", name, before.toString(), null);
    }
}
