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

@CrossOrigin(maxAge = 3600)
public abstract class MetaInfoController implements Controller
{
    abstract String getCollectionName();

    @Inject
    protected Database database;

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Boolean delete(@PathVariable(value = ID) String id)
    {
        database.deleteDocument(getCollectionName(), this, id);
        return true;
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void delete()
    {
        database.deleteDocuments(getCollectionName());
    }

    @GetMapping(produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object get()
    {
        return database.getObjects(getCollectionName(), this, null);
    }
}
