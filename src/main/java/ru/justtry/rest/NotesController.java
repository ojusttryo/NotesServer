package ru.justtry.rest;

import static ru.justtry.shared.NoteConstants.ENTITY;

import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ru.justtry.database.Database;
import ru.justtry.mappers.Mapper;
import ru.justtry.mappers.NoteMapper;
import ru.justtry.notes.Note;
import ru.justtry.validation.NoteValidator;
import ru.justtry.validation.Validator;

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
