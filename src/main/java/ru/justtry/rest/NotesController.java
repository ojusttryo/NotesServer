package ru.justtry.rest;

import static ru.justtry.shared.Constants.ID;
import static ru.justtry.shared.NoteConstants.ENTITY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.server.PathParam;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.justtry.database.Database;
import ru.justtry.database.SortInfo;
import ru.justtry.mappers.NoteMapper;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.Attribute.Type;
import ru.justtry.metainfo.AttributeService;
import ru.justtry.metainfo.Entity;
import ru.justtry.metainfo.EntityService;
import ru.justtry.notes.Note;
import ru.justtry.notes.NoteService;
import ru.justtry.postprocessing.DeleteNotePostprocessor;
import ru.justtry.postprocessing.SaveNotePostprocessor;
import ru.justtry.shared.Identifiable;
import ru.justtry.shared.Utils;
import ru.justtry.validation.NoteValidator;

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
    private NoteService noteService;
    @Autowired
    protected Database database;
    @Autowired
    private SaveNotePostprocessor savePostprocessor;
    @Autowired
    private DeleteNotePostprocessor deletePostprocessor;
    @Autowired
    private Utils utils;
    @Autowired
    private AttributeService attributeService;
    @Autowired
    private EntityService entityService;


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
            noteService.save(entity, note);
            savePostprocessor.process(note, null, entity);
            database.saveLog(getCollectionName(entity), "CREATE", note.getId(), null, note.toString());
            return new ResponseEntity<>(note.getId(), headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            logger.error(e);
            return utils.getResponseForError(headers, e);
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

            Note before = noteService.get(getCollectionName(entity), note.getId());

            noteService.copyUnusedAttributes(note, before);
            noteService.update(entity, note);

            savePostprocessor.process(note, before, entity);
            database.saveLog(entity, "UPDATE", note.getId(), before.toString(), note.toString());
            return new ResponseEntity<>(note.getId(), headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            logger.error(e);
            return utils.getResponseForError(headers, e);
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
            Note note = noteService.get(getCollectionName(entity), id);
            if (note == null)
                throw new IllegalArgumentException(String.format("Note with id=%s in %s not found", id, entity));

            // TODO use savePostprocessor

            Map<String, Attribute> noteAttributes = attributeService.getAttributesAsMap(entity);
            for (String param : params.keySet())
            {
                if (!noteAttributes.containsKey(param))
                    throw new IllegalArgumentException("Note does not contain attribute " + param);

                note.getAttributes().put(param, params.get(param));
            }

            noteService.update(entity, note);
            return new ResponseEntity<>(note.getId(), headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            logger.error(e);
            return utils.getResponseForError(headers, e);
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
            Note note = noteService.get(getCollectionName(entity), id);
            if (note == null)
                throw new IllegalArgumentException("Cannot find note");

            Attribute attribute = attributeService.getByName(attributeName);
            if (attribute == null || !attribute.getType().contentEquals(Type.INC.title))
                throw new IllegalArgumentException("Wrong attribute name");

            if (!note.getAttributes().containsKey(attributeName))
            {
                Entity e = entityService.getByName(entity);
                if (e == null || !e.hasAttribute(attribute.getName()))
                    throw new IllegalArgumentException("This entity has no such attribute");
            }

            Object valueObject = note.getAttributes().get(attributeName);
            Double value;
            if (valueObject == null && attribute.getDefaultValue() == null)
                throw new IllegalArgumentException("Current and default values are absent");
            else if (valueObject == null)
                value = Double.parseDouble(attribute.getDefaultValue());
            else if (valueObject instanceof Double)
                value = (Double)valueObject;
            else if (valueObject instanceof Integer)
                value = Double.parseDouble(((Integer)valueObject).toString());
            else
                value = Double.parseDouble((String)valueObject);

            value += Double.parseDouble(attribute.getStep());

            note.getAttributes().put(attributeName, value);
            noteService.update(entity, note);
            return new ResponseEntity<>(value.toString(), headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            logger.error(e);
            return utils.getResponseForError(headers, e);
        }
    }


    @PutMapping(value = "/{entity}/{id}/hide", consumes = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    public void hide(
            @PathVariable(value = ENTITY) String entity,
            @PathVariable(value = ID) String id)
    {
        noteService.hide(entity, id);
    }


    @PutMapping(value = "/{entity}/{id}/reveal", consumes = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    public void reveal(
            @PathVariable(value = ENTITY) String entity,
            @PathVariable(value = ID) String id)
    {
        noteService.reveal(entity, id);
    }


    @DeleteMapping("/{entity}/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(
            @PathVariable(value = ENTITY) String entity,
            @PathVariable(value = ID) String id)
    {
        Note before = noteService.get(getCollectionName(entity), id);
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


    @GetMapping(path = "/{entity}/hidden", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object getHidden(@PathVariable(value = ENTITY) String entity)
    {
        HttpHeaders headers = new HttpHeaders();
        try
        {
            Entity e = entityService.getByName(entity);
            Identifiable[] objects = noteService.searchByHidden(getCollectionName(entity), true,
                    noteService.createSortInfo(e));
            return new ResponseEntity<>(objects, headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            logger.error(e);
            return utils.getResponseForError(headers, e);
        }
    }


    @GetMapping(path = "/{entity}/visible", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object getVisible(@PathVariable(value = ENTITY) String entity)
    {
        HttpHeaders headers = new HttpHeaders();
        try
        {
            Entity e = entityService.getByName(entity);
            Identifiable[] objects = noteService.searchByHidden(getCollectionName(entity), false,
                    noteService.createSortInfo(e));
            return new ResponseEntity<>(objects, headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            logger.error(e);
            return utils.getResponseForError(headers, e);
        }
    }


    @GetMapping(path = "/nested/{entity}/{attribute}/{parentNoteId}", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object getNested(
            @PathVariable(value = ENTITY) String entity,
            @PathVariable(value = "attribute") String attribute,
            @PathVariable(value = "parentNoteId") String parentNoteId,
            @PathParam(value = "side") String side)
    {
        HttpHeaders headers = new HttpHeaders();
        try
        {
            String nestedValue = (side == null) ? String.format("%s/%s", attribute, parentNoteId)
                    : String.format("%s/%s/%s", attribute, side, parentNoteId);

            Identifiable[] objects = noteService.getNested(entity, nestedValue);
            return new ResponseEntity<>(objects, headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            logger.error(e);
            return utils.getResponseForError(headers, e);
        }
    }


    @PostMapping(path = "/{entity}/search", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object search(
            @RequestBody List<String> ids,
            @PathVariable(value = ENTITY) String entity)
    {
        HttpHeaders headers = new HttpHeaders();
        try
        {
            return noteService.get(entity, ids);
        }
        catch (Exception e)
        {
            logger.error(e);
            return utils.getResponseForError(headers, e);
        }
    }


    @PostMapping(path = "/{entity}/{attributeName}/search", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object search(
            @RequestBody String requestString,
            @PathVariable(value = ENTITY) String entity,
            @PathVariable(value = "attributeName") String attributeName)
    {
        HttpHeaders headers = new HttpHeaders();
        try
        {
            String request = new ObjectMapper().readValue(requestString, String.class);
            Entity e = entityService.getByName(entity);
            SortInfo sortInfo = noteService.createSortInfo(e);
            Attribute attribute = attributeService.getByName(attributeName);
            Attribute.Type type = attribute.getTypeAsEnum();
            Object response = null;
            switch (type)
            {
            case TEXT:
            case TEXT_AREA:
            case DELIMITED_TEXT:
            case URL:
                response = noteService.searchBySubstring(request, getCollectionName(entity), attribute, sortInfo); break;
            case NUMBER:
            case INC:
                response = noteService.searchByNumber(request, getCollectionName(entity), attribute, sortInfo); break;
            case SELECT:
                response = noteService.searchByExactString(request, getCollectionName(entity), attribute, sortInfo); break;
            case CHECKBOX:
                response = noteService.searchByBoolean(request, getCollectionName(entity), attribute, sortInfo); break;
            case MULTI_SELECT:
                response = noteService.searchByIngoing(request, getCollectionName(entity), attribute, sortInfo); break;
            default:
                break;
            }
            return new ResponseEntity<>(response, headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            logger.error(e);
            return utils.getResponseForError(headers, e);
        }
    }


    public String getCollectionName(String entity)
    {
        return entity + ".notes" ;
    }
}
