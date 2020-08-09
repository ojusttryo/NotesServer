package ru.justtry.rest;

import static ru.justtry.shared.Constants.ID;
import static ru.justtry.shared.NoteConstants.ENTITY;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.justtry.database.Database;
import ru.justtry.mappers.AttributeMapper;
import ru.justtry.mappers.EntityMapper;
import ru.justtry.mappers.Mapper;
import ru.justtry.mappers.NoteMapper;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.Attribute.Type;
import ru.justtry.metainfo.Entity;
import ru.justtry.notes.Note;
import ru.justtry.postprocessing.DeleteNotePostprocessor;
import ru.justtry.postprocessing.SaveNotePostprocessor;
import ru.justtry.shared.Identifiable;
import ru.justtry.shared.RestError;
import ru.justtry.validation.NoteValidator;
import ru.justtry.validation.Validator;

@RestController
@RequestMapping("/rest/notes")
public class NotesController extends ObjectsController
{
    final static Logger logger = LogManager.getLogger(NotesController.class);

    @Autowired
    private NoteValidator noteValidator;
    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private AttributeMapper attributeMapper;
    @Autowired
    private EntityMapper entityMapper;
    @Autowired
    protected Database database;
    @Autowired
    private SaveNotePostprocessor savePostprocessor;
    @Autowired
    private DeleteNotePostprocessor deletePostprocessor;


    @PostMapping(value = "/{entity}", consumes = "application/json;charset=UTF-8")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> save(
            @PathVariable(value = ENTITY) String entity,
            @RequestBody Note note)
    {
        HttpHeaders headers = new HttpHeaders();
        try
        {
            noteValidator.validate(note, entity);
            Document document = noteMapper.getDocument(note);
            String id = database.saveDocument(getCollectionName(entity), document);
            note.setId(id);
            savePostprocessor.process(note, entity);
            database.saveLog(getCollectionName(entity), "CREATE", id, null, note.toString());
            return new ResponseEntity<>(id, headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            logger.error(e);
            return new ResponseEntity<>(new RestError(e.getMessage()), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping(value = "/{entity}", consumes = "application/json;charset=UTF-8")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> update(
            @PathVariable(value = ENTITY) String entity,
            @RequestBody Note note)
    {
        HttpHeaders headers = new HttpHeaders();
        try
        {
            noteValidator.validate(note, entity);
            Document document = noteMapper.getDocument(note);
            Identifiable before = noteMapper.getObject(database.getDocument(getCollectionName(entity), note.getId()));
            database.updateDocument(getCollectionName(entity), document);
            savePostprocessor.process(note, entity);
            database.saveLog(entity, "UPDATE", note.getId(), before.toString(), note.toString());
            return new ResponseEntity<>(note.getId(), headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            logger.error(e);
            return new ResponseEntity<>(new RestError(e.getMessage()), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(value = "/{entity}/{id}", consumes = "application/json;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<Object> update(
            @RequestBody String json,
            @PathVariable(value = ENTITY) String entity,
            @PathVariable(value = "id") String id)
    {
        HttpHeaders headers = new HttpHeaders();
        try
        {
            Map<String, Object> params = new ObjectMapper().readValue(json, HashMap.class);

            Note note = (Note)noteMapper.getObject(database.getDocument(getCollectionName(entity), id));
            if (note == null)
                throw new IllegalArgumentException(String.format("Note with id=%s in %s not found", id, entity));

            for (String param : params.keySet())
            {
                if (note.getAttributes().containsKey(param))
                    note.getAttributes().put(param, params.get(param));
            }

            database.updateDocument(getCollectionName(entity), noteMapper.getDocument(note));
            return new ResponseEntity<>(note.getId(), headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            logger.error(e);
            return new ResponseEntity<>(new RestError(e.getMessage()), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping(value = "/{entity}/{id}/inc/{attributeName}", consumes = "application/json;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<Object> inc(
            @PathVariable(value = ENTITY) String entity,
            @PathVariable(value = "id") String id,
            @PathVariable(value = "attributeName") String attributeName)
    {
        HttpHeaders headers = new HttpHeaders();
        try
        {
            Note note = (Note)noteMapper.getObject(database.getDocument(getCollectionName(entity), id));
            if (note == null)
                throw new IllegalArgumentException("Wrong note id");

            Document attributeDoc = database.getAttribute(attributeName);
            Attribute attribute = (Attribute)attributeMapper.getObject(attributeDoc);
            if (attribute == null || !attribute.getType().contentEquals(Type.INC.title))
                throw new IllegalArgumentException("Wrong attribute name");

            if (!note.getAttributes().containsKey(attributeName))
            {
                Entity e = (Entity)entityMapper.getObject(database.getEntity(entity));
                if (e == null || e.getAttributes().stream().allMatch(x -> !x.contentEquals(attribute.getId())))
                    throw new IllegalArgumentException("This entity has no such attribute");
            }

            Object valueObject = note.getAttributes().get(attributeName);
            Double value;
            if (valueObject == null && attribute.getDefaultValue() == null)
                throw new IllegalArgumentException("Current and default values are absent");
            else if (valueObject == null)
                value = Double.parseDouble(attribute.getDefaultValue());
            else
                value = (valueObject instanceof Double) ? (Double)valueObject : Double.parseDouble((String)valueObject);

            value += Double.parseDouble(attribute.getStep());

            note.getAttributes().put(attributeName, value);
            database.updateDocument(getCollectionName(entity), noteMapper.getDocument(note));
            return new ResponseEntity<>(value.toString(), headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            logger.error(e);
            return new ResponseEntity<>(new RestError(e.getMessage()), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/{entity}/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(
            @PathVariable(value = ENTITY) String entity,
            @PathVariable(value = ID) String id)
    {
        Note before = (Note)noteMapper.getObject(database.getDocument(getCollectionName(entity), id));
        database.deleteDocument(getCollectionName(entity), id);
        deletePostprocessor.process(before, entity);
        database.saveLog(getCollectionName(entity), "DELETE", id, before.toString(), null);
    }


    @DeleteMapping("/{entity}")
    @ResponseStatus(HttpStatus.OK)
    public void dropCollection(@PathVariable(value = ENTITY) String entity)
    {
        long count = database.dropCollection(getCollectionName(entity));
        // TODO unlink here all files related to entity (new DB method)
        database.saveLog(entity, "DELETE", null, count, 0);
    }


    @Override
    public Validator getValidator()
    {
        return noteValidator;
    }


    @Override
    public Mapper getMapper()
    {
        return noteMapper;
    }


    @Override
    public String getCollectionName(String entity)
    {
        return entity + ".notes" ;
    }
}
