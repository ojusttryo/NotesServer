package ru.justtry.rest;

import static ru.justtry.shared.NoteConstants.ENTITY;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import ru.justtry.mappers.Mapper;
import ru.justtry.mappers.NoteMapper;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.Attribute.Type;
import ru.justtry.metainfo.Entity;
import ru.justtry.notes.Note;
import ru.justtry.shared.RestError;
import ru.justtry.validation.NoteValidator;
import ru.justtry.validation.Validator;

@RestController
@RequestMapping("/rest/notes")
public class NotesController extends ObjectsController
{
    @Inject
    private NoteValidator noteValidator;
    @Inject
    private NoteMapper noteMapper;
    @Inject
    protected Database database;


    @PostMapping(value = "/{entity}", consumes = "application/json;charset=UTF-8")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> save(@PathVariable(value = ENTITY) String entity,
                       @RequestBody Note note)
    {
        HttpHeaders headers = new HttpHeaders();
        try
        {
            String id = database.saveDocument(getCollectionName(entity), noteValidator, noteMapper, note);
            return new ResponseEntity<>(id, headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(new RestError(e.getMessage()), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping(value = "/{entity}", consumes = "application/json;charset=UTF-8")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> update(@PathVariable(value = ENTITY) String entity,
                       @RequestBody Note note)
    {
        HttpHeaders headers = new HttpHeaders();
        try
        {
            database.updateDocument(getCollectionName(entity), noteValidator, noteMapper, note);
            return new ResponseEntity<>(note.getId(), headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
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

            Note note = (Note)database.getObject(getCollectionName(entity), noteMapper, id);
            if (note == null)
                throw new IllegalArgumentException("Wrong note id");

            for (String param : params.keySet())
            {
                if (note.getAttributes().containsKey(param))
                    note.getAttributes().put(param, params.get(param));
            }

            database.updateDocument(getCollectionName(entity), noteValidator, noteMapper, note);
            return new ResponseEntity<>(note.getId(), headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
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
            Note note = (Note)database.getObject(getCollectionName(entity), noteMapper, id);
            if (note == null)
                throw new IllegalArgumentException("Wrong note id");

            Attribute attribute = database.getAttribute(attributeName);
            if (attribute == null || !attribute.getType().contentEquals(Type.INC.title))
                throw new IllegalArgumentException("Wrong attribute name");

            if (!note.getAttributes().containsKey(attributeName))
            {
                Entity e = database.getEntity(entity);
                if (e == null || e.getAttributes().stream().allMatch(x -> !x.contentEquals(attribute.getId())))
                    throw new IllegalArgumentException("This entity has no such attribute");
            }

            Object valueObject = note.getAttributes().get(attributeName);
            Double value = null;
            if (valueObject == null && attribute.getDefaultValue() == null)
                throw new IllegalArgumentException("Current and default values are absent");
            else if (valueObject == null)
                value = Double.parseDouble(attribute.getDefaultValue());
            else
                value = (valueObject instanceof Double) ? (Double)valueObject : Double.parseDouble((String)valueObject);

            value += Double.parseDouble(attribute.getStep());

            note.getAttributes().put(attributeName, value);
            database.updateDocument(getCollectionName(entity), noteValidator, noteMapper, note);
            return new ResponseEntity<>(value.toString(), headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(new RestError(e.getMessage()), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    protected Validator getValidator()
    {
        return noteValidator;
    }


    @Override
    protected Mapper getMapper()
    {
        return noteMapper;
    }


    @Override
    protected String getCollectionName(String entity)
    {
        return String.format("%s.notes", entity);
    }
}
