package ru.justtry.rest;

import static ru.justtry.shared.AttributeConstants.ATTRIBUTES_COLLECTION;
import static ru.justtry.shared.Constants.ID;
import static ru.justtry.shared.RestConstants.REST_ATTRIBUTES;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import ru.justtry.mappers.AttributeMapper;
import ru.justtry.mappers.Mapper;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.AttributeService;
import ru.justtry.metainfo.Entity;
import ru.justtry.metainfo.EntityService;
import ru.justtry.postprocessing.DeleteAttributePostprocessor;
import ru.justtry.postprocessing.SaveAttributePostprocessor;
import ru.justtry.shared.Utils;
import ru.justtry.validation.AttributeValidator;
import ru.justtry.validation.Validator;


@RestController
@RequestMapping(REST_ATTRIBUTES)
public class AttributesController extends MetaInfoController
{
    final static Logger logger = LogManager.getLogger(AttributesController.class);

    @Autowired
    private AttributeValidator attributeValidator;
    @Autowired
    private AttributeMapper attributeMapper;
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
            attributeValidator.validate(attribute, ATTRIBUTES_COLLECTION);
            Attribute before = attributeService.getById(attribute.getId());
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
            @RequestParam(value = "entityId", required = false) String entityId,
            @RequestParam(value = ID, required = false) String id)
    {
        if (entityId != null)
        {
            Entity entity = entityService.getById(entityId);
            return attributeService.get(entity.getAttributes());
        }
        else if (id != null)
        {
            return attributeService.getById(id);
        }
        else
        {
            return attributeService.getAll();
        }
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable(value = ID) String id)
    {
        Attribute before = attributeService.getById(id);
        database.deleteDocument(ATTRIBUTES_COLLECTION, id);
        deletePostprocessor.process(before);
        database.saveLog(ATTRIBUTES_COLLECTION, "DELETE", id, before.toString(), null);
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
