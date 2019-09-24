package ru.justtry.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.justtry.database.Database;
import ru.justtry.notes.Note;
import ru.justtry.notes.NoteMapper;
import ru.justtry.notes.NoteValidator;

import javax.inject.Inject;

import static ru.justtry.notes.NoteConstants.ENTITY;
import static ru.justtry.shared.Constants.ID;

@RestController
@RequestMapping("/rest/notes")
public class NotesController
{
    @Inject
    private NoteValidator noteValidator;
    @Inject
    private NoteMapper noteMapper;
    @Inject
    protected Database database;

    @PostMapping(value = "/{entity}", consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public String save(@PathVariable(value = ENTITY) String entity,
                       @RequestBody Note note)
    {
        return database.saveDocument(getCollectionName(entity), noteValidator, noteMapper, note);
    }

    @PutMapping(value = "/{entity}", consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable(value = ENTITY) String entity,
                       @RequestBody Note note)
    {
        database.updateDocument(getCollectionName(entity), noteValidator, noteMapper, note);
    }

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
        return database.getObject(getCollectionName(entity), noteMapper, id);
    }

    @GetMapping("/{entity}")
    @ResponseBody
    public Object[] getAll(@PathVariable(value = ENTITY) String entity)
    {
        return database.getObjects(getCollectionName(entity), noteMapper);
    }


    private String getCollectionName(String entity)
    {
        return String.format("%s.notes", entity);
    }

}
