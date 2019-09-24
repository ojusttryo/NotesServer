package ru.justtry.shared;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.justtry.database.Database;

import javax.inject.Inject;

import static ru.justtry.attributes.EntityConstants.ENTITIES_COLLECTION;
import static ru.justtry.shared.Constants.ID;
import static ru.justtry.shared.Constants.MONGO_ID;

public abstract class Controller
{
    protected abstract Mapper getMapper();
    protected abstract Validator getValidator();
    protected abstract String getCollectionName();

    @Inject
    protected Database database;

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable(value = ID) String id)
    {
        database.deleteDocument(getCollectionName(), id);
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
    public Object[] getAll()
    {
        return database.getObjects(getCollectionName(), getMapper());
    }
}
