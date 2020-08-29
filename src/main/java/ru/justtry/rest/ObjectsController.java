package ru.justtry.rest;

import static ru.justtry.shared.Constants.ID;
import static ru.justtry.shared.NoteConstants.ENTITY;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.bson.Document;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import ru.justtry.database.Database;
import ru.justtry.shared.Identifiable;

@CrossOrigin(maxAge = 3600)
public abstract class ObjectsController implements Controller
{
    abstract String getCollectionName(String entity);

    @Inject
    protected Database database;


    @GetMapping("/{entity}/{id}")
    @ResponseBody
    public Identifiable get(@PathVariable(value = ENTITY) String entity,
                      @PathVariable(value = ID) String id)
    {
        return getMapper().getObject(database.getDocument(getCollectionName(entity), id));
    }


    @GetMapping("/{entity}")
    @ResponseBody
    public ResponseEntity<Identifiable[]> getAll(@PathVariable(value = ENTITY) String entity)
    {
        HttpHeaders headers = new HttpHeaders();
        List<Document> documents = database.getDocuments(getCollectionName(entity), new ArrayList<>());
        return new ResponseEntity<>(getMapper().getObjects(documents), headers, HttpStatus.OK);
    }
}
