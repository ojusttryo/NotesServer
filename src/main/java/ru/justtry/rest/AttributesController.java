package ru.justtry.rest;

import static ru.justtry.shared.AttributeConstants.ATTRIBUTES_COLLECTION;
import static ru.justtry.shared.Constants.ID;
import static ru.justtry.shared.RestConstants.REST_ATTRIBUTES;

import javax.inject.Inject;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ru.justtry.mappers.AttributeMapper;
import ru.justtry.mappers.Mapper;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.Entity;
import ru.justtry.shared.RestError;
import ru.justtry.validation.AttributeValidator;
import ru.justtry.validation.Validator;


@RestController
@RequestMapping(REST_ATTRIBUTES)
public class AttributesController extends MetaInfoController
{
    @Inject
    private AttributeValidator attributeValidator;
    @Inject
    private AttributeMapper attributeMapper;
    @Inject
    private EntitiesController entitiesController;


    @PostMapping(consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<Object> save(@RequestBody Attribute attribute)
    {
        HttpHeaders headers = new HttpHeaders();
        try
        {
            String id = database.saveDocument(ATTRIBUTES_COLLECTION, getValidator(), getMapper(), attribute);
            return new ResponseEntity<>(id, headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(new RestError(e.getMessage()), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> update(@RequestBody Attribute attribute)
    {
        HttpHeaders headers = new HttpHeaders();
        try
        {
            database.updateDocument(ATTRIBUTES_COLLECTION, getValidator(), getMapper(), attribute);
            return new ResponseEntity<>(attribute.getId(), headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(new RestError(e.getMessage()), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping(path = "/search", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object get(
            @RequestParam(value = "entityId", required = false) String entityId,
            @RequestParam(value = ID, required = false) String id)
    {
        if (entityId != null)
        {
            Entity entity = (Entity)entitiesController.get(entityId);
            return database.getObjects(ATTRIBUTES_COLLECTION, getMapper(), entity.getAttributes());
        }
        else if (id != null)
        {
            return database.getObject(getCollectionName(), getMapper(), id);
        }
        else
        {
            return database.getObjects(getCollectionName(), getMapper(), null);
        }
    }

    @Override
    public Mapper getMapper()
    {
        return attributeMapper;
    }

    @Override
    public Validator getValidator()
    {
        return attributeValidator;
    }

    @Override
    public String getCollectionName()
    {
        return ATTRIBUTES_COLLECTION;
    }
}
