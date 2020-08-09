package ru.justtry.rest;

import static ru.justtry.shared.Constants.ID;
import static ru.justtry.shared.NoteConstants.ENTITY;

import javax.inject.Inject;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import ru.justtry.database.Database;


@CrossOrigin(maxAge = 3600)
public abstract class ObjectsController implements Controller
{
    abstract String getCollectionName(String entity);

    @Inject
    protected Database database;


    @DeleteMapping("/{entity}/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable(value = ENTITY) String entity,
                       @PathVariable(value = ID) String id)
    {
        database.deleteDocument(getCollectionName(entity), this, id);
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
        return database.getObject(getCollectionName(entity), this, id);
    }


    @GetMapping("/{entity}")
    @ResponseBody
    public ResponseEntity<Object[]> getAll(@PathVariable(value = ENTITY) String entity)
    {
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(database.getObjects(getCollectionName(entity), this, null), headers,
                HttpStatus.OK);
    }
}
