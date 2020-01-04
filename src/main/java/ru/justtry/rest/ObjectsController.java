package ru.justtry.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.justtry.database.Database;
import ru.justtry.mappers.FolderMapper;
import ru.justtry.mappers.Mapper;
import ru.justtry.notes.Note;
import ru.justtry.validation.FolderValidator;
import ru.justtry.validation.Validator;

import javax.inject.Inject;

import static ru.justtry.shared.Constants.ID;
import static ru.justtry.shared.NoteConstants.ENTITY;


public abstract class ObjectsController
{
    protected abstract Validator getValidator();
    protected abstract Mapper getMapper();
    protected abstract String getCollectionName(String entity);

    @Inject
    protected Database database;


    @DeleteMapping("/{entity}/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable(value = ENTITY) String entity,
                       @PathVariable(value = ID) String id)
    {
        database.deleteDocument(getCollectionName(entity), id);
    }

    @DeleteMapping("/{entity}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteAll(@PathVariable(value = ENTITY) String entity)
    {
        database.deleteDocuments(getCollectionName(entity));
    }

    @GetMapping("/{entity}/{id}")
    @ResponseBody
    public Object get(@PathVariable(value = ENTITY) String entity,
                      @PathVariable(value = ID) String id)
    {
        return database.getObject(getCollectionName(entity), getMapper(), id);
    }

//    @GetMapping("/{entity}")
//    @ResponseBody
//    public Object[] getAll(@PathVariable(value = ENTITY) String entity)
//    {
//        return database.getObjects(getCollectionName(entity), getMapper());
//    }

    @GetMapping("/{entity}")
    @ResponseBody
    public ResponseEntity<Object[]> getAll(@PathVariable(value = ENTITY) String entity)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Access-Control-Allow-Credentials", "true");
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        return new ResponseEntity<>(database.getObjects(getCollectionName(entity), getMapper()), headers, HttpStatus.OK);
    }
}
