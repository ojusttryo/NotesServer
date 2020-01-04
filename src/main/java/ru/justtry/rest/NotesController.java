package ru.justtry.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.justtry.database.Database;
import ru.justtry.mappers.Mapper;
import ru.justtry.notes.Note;
import ru.justtry.mappers.NoteMapper;
import ru.justtry.validation.NoteValidator;
import ru.justtry.validation.Validator;

import javax.inject.Inject;

import static ru.justtry.shared.NoteConstants.ENTITY;
import static ru.justtry.shared.Constants.ID;

@RestController
@RequestMapping("/rest/notes")
public class NotesController extends ObjectsController
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


    @Override
    protected Validator getValidator()
    {
        return noteValidator;
    }


    @Override
    protected Mapper getMapper()
    {
        return noteMapper;
    }


    @Override
    protected String getCollectionName(String entity)
    {
        return String.format("%s.notes", entity);
    }

}
