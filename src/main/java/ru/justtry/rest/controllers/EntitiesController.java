package ru.justtry.rest.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static ru.justtry.shared.EntityConstants.ENTITIES_COLLECTION;

import org.bson.Document;
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
import ru.justtry.mappers.EntityMapper;
import ru.justtry.metainfo.Entity;
import ru.justtry.metainfo.EntityService;
import ru.justtry.notes.NoteService;
import ru.justtry.shared.Identifiable;
import ru.justtry.validation.delete.DeleteEntityValidator;
import ru.justtry.validation.save.SaveEntityValidator;

@RestController
@CrossOrigin(maxAge = 3600)
@RequestMapping("/rest/entities")
@Slf4j
@RequiredArgsConstructor
public class EntitiesController
{
    private final Database database;
    private final EntityMapper entityMapper;
    private final SaveEntityValidator saveEntityValidator;
    private final DeleteEntityValidator deleteEntityValidator;
    private final EntityService entityService;
    private final NoteService noteService;


    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> save(@RequestBody Entity entity)
    {
        saveEntityValidator.validate(entity, ENTITIES_COLLECTION);
        Document document = entityMapper.getDocument(entity);
        String id = database.saveDocument(ENTITIES_COLLECTION, document);
        entity.setId(id);
        database.saveLog(ENTITIES_COLLECTION, "CREATE", id, null, entity.toString());
        return new ResponseEntity<>(id, new HttpHeaders(), HttpStatus.OK);
    }


    @PutMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> update(@RequestBody Entity entity)
    {
        Identifiable before = entityService.getByName(entity.getName());
        entity.setId(before.getId());
        saveEntityValidator.validate(entity, ENTITIES_COLLECTION);
        entityService.update(entity);
        database.saveLog(ENTITIES_COLLECTION, "UPDATE", entity.getId(), before.toString(), entity.toString());
        return new ResponseEntity<>(entity.getId(), new HttpHeaders(), HttpStatus.OK);
    }


    @GetMapping(path = "/search", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Object get(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String attribute)
    {
        if (id != null)
            return entityService.getById(id);
        else if (name != null)
            return entityService.getByName(name);
        else if (attribute != null)
            return entityService.getByAttribute(attribute);
        else
            return entityService.getAll();
    }


    @GetMapping(path = "/id/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Entity getById(@PathVariable(required = false) String id)
    {
        return entityService.getById(id);
    }

    @GetMapping(path = "/name/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Entity getByName(@PathVariable(required = false) String name)
    {
        return entityService.getByName(name);
    }


    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Entity[] get()
    {
        return entityService.getAll();
    }


    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable String name)
    {
        Entity entity = entityService.getByName(name);
        deleteEntityValidator.validate(entity, ENTITIES_COLLECTION);
        database.deleteDocument(ENTITIES_COLLECTION, entity.getId());
        database.dropCollection(noteService.getCollectionName(name));
        database.saveLog(ENTITIES_COLLECTION, "DELETE", name, entity.toString(), null);
    }

}
