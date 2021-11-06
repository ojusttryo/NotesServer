package ru.justtry.rest.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static ru.justtry.shared.AttributeConstants.ATTRIBUTES_COLLECTION;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.justtry.database.Database;
import ru.justtry.database.LogRecord.Operation;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.AttributeService;
import ru.justtry.metainfo.Entity;
import ru.justtry.metainfo.EntityService;
import ru.justtry.postprocessing.DeleteAttributePostprocessor;
import ru.justtry.postprocessing.SaveAttributePostprocessor;
import ru.justtry.validation.delete.DeleteAttributeValidator;
import ru.justtry.validation.save.SaveAttributeValidator;


@RestController
@CrossOrigin(maxAge = 3600)
@RequestMapping("/rest/attributes")
@Slf4j
@RequiredArgsConstructor
public class AttributesController
{
    private final Database database;
    private final SaveAttributeValidator saveAttributeValidator;
    private final DeleteAttributeValidator deleteAttributeValidator;
    private final SaveAttributePostprocessor savePostprocessor;
    private final DeleteAttributePostprocessor deletePostprocessor;
    private final AttributeService attributeService;
    private final EntityService entityService;


    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> save(@RequestBody Attribute attribute)
    {
        saveAttributeValidator.validate(attribute, ATTRIBUTES_COLLECTION);
        attributeService.save(attribute);
        savePostprocessor.process(attribute);
        database.saveLog(ATTRIBUTES_COLLECTION, Operation.CREATE, attribute.getId(), null, attribute.toString());
        return new ResponseEntity<>(attribute.getId(), new HttpHeaders(), HttpStatus.OK);
    }


    @PutMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> update(@RequestBody Attribute attribute)
    {
        attribute.setId(attributeService.getId(attribute.getName()));
        saveAttributeValidator.validate(attribute, ATTRIBUTES_COLLECTION);
        Attribute before = attributeService.getByName(attribute.getName());
        attributeService.update(attribute);
        savePostprocessor.process(attribute);
        database.saveLog(ATTRIBUTES_COLLECTION, Operation.UPDATE, attribute.getId(), before.toString(), attribute.toString());
        return new ResponseEntity<>(attribute.getId(), new HttpHeaders(), HttpStatus.OK);
    }


    @GetMapping(path = "/search", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Object get(
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean visible,
            @RequestParam(required = false) Boolean shared)
    {
        // all by shared
        if (shared != null && shared)
        {
            if (entityName != null)
            {
                Entity entity = entityService.getByName(entityName);
                return attributeService.getAvailable(entity);
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
            // TODO вынести в отдельный метод, чтоб здесь возвращать Attribute[]
            return attributeService.getByName(name);
        }
        // all
        else
        {
            return attributeService.getAll();
        }
    }


    @GetMapping(path = "/compared/{entityName}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Object get(@PathVariable String entityName)
    {
        Entity entity = entityService.getByName(entityName);
        return attributeService.get(entity.getComparedAttributes());
    }


    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Attribute[] get()
    {
        return attributeService.getAllWithUsage();
    }


    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable String name) throws IllegalStateException
    {
        Attribute attribute = attributeService.getByName(name);
        deleteAttributeValidator.validate(attribute, ATTRIBUTES_COLLECTION);
        database.deleteDocument(ATTRIBUTES_COLLECTION, attribute.getId());
        deletePostprocessor.process(attribute);
        database.saveLog(ATTRIBUTES_COLLECTION, Operation.DELETE, name, attribute.toString(), null);
    }
}


