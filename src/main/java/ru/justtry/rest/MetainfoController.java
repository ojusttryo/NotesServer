package ru.justtry.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.justtry.database.Database;
import ru.justtry.mappers.Mapper;
import ru.justtry.validation.Validator;

import javax.inject.Inject;

import static ru.justtry.shared.Constants.ID;
import static ru.justtry.shared.NoteConstants.ENTITY;

public abstract class MetainfoController
{
    protected abstract Mapper getMapper();
    protected abstract Validator getValidator();
    protected abstract String getCollectionName();

    @Inject
    protected Database database;

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> delete(@PathVariable(value = ID) String id)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Access-Control-Allow-Credentials", "true");
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        database.deleteDocument(getCollectionName(), id);

        return new ResponseEntity<Void>(null, headers, HttpStatus.OK);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void deleteAll()
    {
        database.deleteDocuments(getCollectionName());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public Object get(@PathVariable(value = ID) String id)
    {
        return database.getObject(getCollectionName(), getMapper(), id);
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<Object[]> getAll()
    {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Access-Control-Allow-Credentials", "true");
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        return new ResponseEntity<>(database.getObjects(getCollectionName(), getMapper()), headers, HttpStatus.OK);
    }
}
