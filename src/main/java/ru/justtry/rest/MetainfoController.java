package ru.justtry.rest;

import static ru.justtry.shared.Constants.ID;

import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import ru.justtry.database.Database;
import ru.justtry.mappers.Mapper;
import ru.justtry.validation.Validator;

@CrossOrigin(maxAge = 3600)
public abstract class MetaInfoController
{
    public abstract Mapper getMapper();
    public abstract Validator getValidator();
    public abstract String getCollectionName();

    @Inject
    protected Database database;

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Boolean delete(@PathVariable(value = ID) String id)
    {
        database.deleteDocument(getCollectionName(), id);
        return true;
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void deleteAll()
    {
        database.deleteDocuments(getCollectionName());
    }

    @GetMapping(path = "/{id}", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Object get(@PathVariable(value = ID) String id)
    {
        return database.getObject(getCollectionName(), getMapper(), id);
    }

    @GetMapping(produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object[] getAll()
    {
        return database.getObjects(getCollectionName(), getMapper());
    }
}
