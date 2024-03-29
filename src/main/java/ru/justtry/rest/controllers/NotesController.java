package ru.justtry.rest.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.server.PathParam;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.justtry.database.Database;
import ru.justtry.database.LogRecord.Operation;
import ru.justtry.database.sort.SortInfo;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.dictionary.Type;
import ru.justtry.metainfo.AttributeService;
import ru.justtry.metainfo.Entity;
import ru.justtry.metainfo.EntityService;
import ru.justtry.notes.Note;
import ru.justtry.notes.NoteService;
import ru.justtry.postprocessing.DeleteNotePostprocessor;
import ru.justtry.postprocessing.SaveNotePostprocessor;
import ru.justtry.shared.Identifiable;
import ru.justtry.shared.Utils;
import ru.justtry.validation.save.SaveNoteValidator;

@RestController
@RequestMapping("/rest/notes")
@Slf4j
@RequiredArgsConstructor
public class NotesController
{
    private final SaveNoteValidator saveNoteValidator;
    private final NoteService noteService;
    private final Database database;
    private final SaveNotePostprocessor savePostprocessor;
    private final DeleteNotePostprocessor deletePostprocessor;
    private final Utils utils;
    private final AttributeService attributeService;
    private final EntityService entityService;


    @PostMapping(value = "/{entity}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> save(@PathVariable String entity, @RequestBody Note note)
    {
        saveNoteValidator.validate(note, entity);
        noteService.save(entity, note);
        savePostprocessor.process(note, null, entity);
        database.saveLog(noteService.getCollectionName(entity), Operation.CREATE, note.getId(), null, note.toString());
        return new ResponseEntity<>(note.getId(), new HttpHeaders(), HttpStatus.OK);
    }


    @PutMapping(value = "/{entity}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> update(@PathVariable String entity, @RequestBody Note note)
    {
        saveNoteValidator.validate(note, entity);

        Note before = noteService.get(noteService.getCollectionName(entity), note.getId());
        note.setFavorite(before.isFavorite());
        note.setHidden(before.isHidden());

        Map<String, Attribute> attributes = attributeService.getAttributesAsMap(entity);
        noteService.copyUnusedAttributes(attributes, note, before);
        noteService.copyTimeAttributes(attributes, note, before);
        noteService.update(entity, note);

        savePostprocessor.process(note, before, entity);
        database.saveLog(entity, Operation.UPDATE, note.getId(), before.toString(), note.toString());
        return new ResponseEntity<>(note.getId(), new HttpHeaders(), HttpStatus.OK);
    }


    @PutMapping(value = "/{entity}/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> update(
            @RequestBody String json,
            @PathVariable String entity,
            @PathVariable String id) throws JsonProcessingException
    {
        Map<String, Object> params = new ObjectMapper().readValue(json, HashMap.class);
        // Two same request in order not to use some deep copy lib
        Note before = noteService.get(noteService.getCollectionName(entity), id);
        Note note = noteService.get(noteService.getCollectionName(entity), id);
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
        savePostprocessor.process(note, before, entity);

        return new ResponseEntity<>(note.getId(), new HttpHeaders(), HttpStatus.OK);
    }


    @PutMapping(value = "/{entity}/{id}/inc/{attributeName}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> inc(
            @PathVariable String entity,
            @PathVariable String id,
            @PathVariable String attributeName)
    {
        // Two same request in order not to use some deep copy lib
        Note before = noteService.get(noteService.getCollectionName(entity), id);
        Note note = noteService.get(noteService.getCollectionName(entity), id);
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
        // At this point of time it might be not necessary to use the post processor after incrementing one
        // attribute. But later there might be something to process. So let's leave it here in order not to forget.
        savePostprocessor.process(note, before, entity);

        return new ResponseEntity<>(value.toString(), new HttpHeaders(), HttpStatus.OK);
    }


    @PutMapping(value = "/{entity}/{id}/hide", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void hide(@PathVariable String entity, @PathVariable String id)
    {
        noteService.hide(entity, id);
    }


    @PutMapping(value = "/{entity}/{id}/reveal", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void reveal(@PathVariable String entity, @PathVariable String id)
    {
        noteService.reveal(entity, id);
    }


    @DeleteMapping(value = "/{entity}/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable String entity, @PathVariable String id)
    {
        Note before = noteService.get(noteService.getCollectionName(entity), id);
        database.deleteDocument(noteService.getCollectionName(entity), id);
        deletePostprocessor.process(before, entity);
        database.saveLog(noteService.getCollectionName(entity), Operation.DELETE, id, before.toString(), null);
    }


    @GetMapping(value = "/{entity}/{id}/key", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getKey(@PathVariable String entity, @PathVariable String id)
    {
        return new ResponseEntity<>(noteService.getKey(entity, id), new HttpHeaders(), HttpStatus.OK);
    }


    @DeleteMapping(value = "/{entity}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void dropCollection(@PathVariable String entity)
    {
        long count = database.dropCollection(noteService.getCollectionName(entity));
        // TODO unlink here all files related to entity (new DB method)
        database.saveLog(entity, Operation.DELETE, null, count, 0);
    }


    @GetMapping(path = "/{entity}/hidden", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getHidden(@PathVariable String entity)
    {
        Entity e = entityService.getByName(entity);
        Identifiable[] objects = noteService.searchByHidden(noteService.getCollectionName(entity), true,
                noteService.createSortInfo(e));
        return new ResponseEntity<>(objects, new HttpHeaders(), HttpStatus.OK);
    }


    @GetMapping(path = "/{entity}/visible", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getVisible(@PathVariable String entity)
    {
        Entity e = entityService.getByName(entity);
        Identifiable[] objects = noteService.searchByHidden(noteService.getCollectionName(entity), false,
                noteService.createSortInfo(e));
        return new ResponseEntity<>(objects, new HttpHeaders(), HttpStatus.OK);
    }


    @GetMapping(path = "/nested/{entity}/{attribute}/{parentNoteId}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> getNested(
            @PathVariable String entity,
            @PathVariable String attribute,
            @PathVariable String parentNoteId,
            @PathParam(value = "side") String side)
    {
        String nestedValue = (side == null) ? String.format("%s/%s", attribute, parentNoteId)
                : String.format("%s/%s/%s", attribute, side, parentNoteId);
        Identifiable[] objects = noteService.getNested(entity, nestedValue);
        return new ResponseEntity<>(objects, new HttpHeaders(), HttpStatus.OK);
    }


    @PostMapping(path = "/{entity}/search", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> search(@RequestBody List<String> ids, @PathVariable String entity)
    {
        return new ResponseEntity<>(noteService.get(entity, ids), new HttpHeaders(), HttpStatus.OK);
    }


    @PostMapping(path = "/{entity}/{attributeName}/search", consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> search(
            @RequestBody String requestString,
            @PathVariable String entity,
            @PathVariable String attributeName) throws JsonProcessingException
    {
        String request = new ObjectMapper().readValue(requestString, String.class);
        Entity e = entityService.getByName(entity);
        SortInfo sortInfo = noteService.createSortInfo(e);
        Attribute attribute = attributeService.getByName(attributeName);
        Type type = attribute.getTypeAsEnum();
        Object response;

        switch (type)
        {
        case TEXT:
        case TEXT_AREA:
        case DELIMITED_TEXT:
        case URL:
            response = noteService.searchBySubstring(request, noteService.getCollectionName(entity), attribute, sortInfo);
            break;
        case NUMBER:
        case INC:
            response = noteService.searchByNumber(request, noteService.getCollectionName(entity), attribute, sortInfo);
            break;
        case SELECT:
            response = noteService.searchByExactString(request, noteService.getCollectionName(entity), attribute, sortInfo);
            break;
        case CHECKBOX:
            response = noteService.searchByBoolean(request, noteService.getCollectionName(entity), attribute, sortInfo);
            break;
        case MULTI_SELECT:
            response = noteService.searchByIngoing(request, noteService.getCollectionName(entity), attribute, sortInfo);
            break;
        default:
            throw new IllegalArgumentException("Wrong attribute type");
        }

        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
    }


    @GetMapping("/{entity}/{id}")
    public Identifiable get(@PathVariable String entity, @PathVariable String id)
    {
        return noteService.get(noteService.getCollectionName(entity), id);
    }


    @GetMapping("/{entity}")
    public ResponseEntity<Identifiable[]> getAll(@PathVariable String entity)
    {
        HttpHeaders headers = new HttpHeaders();
        Identifiable[] objects = noteService.getRegular(entity);
        return new ResponseEntity<>(objects, headers, HttpStatus.OK);
    }

}
