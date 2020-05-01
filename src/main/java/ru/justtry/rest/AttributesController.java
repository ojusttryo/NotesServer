package ru.justtry.rest;

import static ru.justtry.shared.AttributeConstants.ATTRIBUTES_COLLECTION;
import static ru.justtry.shared.Constants.ID;
import static ru.justtry.shared.RestConstants.REST_ATTRIBUTES;

import javax.inject.Inject;
import javax.websocket.server.PathParam;

import org.springframework.http.HttpStatus;
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


    @PostMapping(consumes = "application/json;charset=UTF-8", produces = "text/plain;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String save(@RequestBody Attribute attribute)
    {
        return database.saveDocument(ATTRIBUTES_COLLECTION, getValidator(), getMapper(), attribute);
    }

    @PutMapping(consumes = "application/json;charset=UTF-8", produces = "text/plain;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    public String update(@RequestBody Attribute attribute)
    {
        database.updateDocument(ATTRIBUTES_COLLECTION, getValidator(), getMapper(), attribute);
        return attribute.getId();
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
