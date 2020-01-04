package ru.justtry.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.justtry.database.Database;
import ru.justtry.mappers.FolderMapper;
import ru.justtry.mappers.Mapper;
import ru.justtry.notes.Note;
import ru.justtry.notes.NoteFolder;
import ru.justtry.validation.FolderValidator;
import ru.justtry.validation.Validator;

import javax.inject.Inject;

import static ru.justtry.shared.NoteConstants.ENTITY;

@RestController
@RequestMapping("/rest/folders")
public class FoldersController extends ObjectsController
{
    @Inject
    private FolderValidator folderValidator;
    @Inject
    private FolderMapper folderMapper;
    @Inject
    protected Database database;


    @PostMapping(value = "/{entity}", consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public String save(@PathVariable(value = ENTITY) String entity,
                       @RequestBody NoteFolder folder)
    {
        return database.saveDocument(getCollectionName(entity), getValidator(), getMapper(), folder);
    }


    @PutMapping(value = "/{entity}", consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable(value = ENTITY) String entity,
                       @RequestBody NoteFolder folder)
    {
        database.updateDocument(getCollectionName(entity), getValidator(), getMapper(), folder);
    }


    @Override
    protected Validator getValidator()
    {
        return folderValidator;
    }


    @Override
    protected Mapper getMapper()
    {
        return folderMapper;
    }


    @Override
    protected String getCollectionName(String entity)
    {
        return String.format("%s.folder", entity);
    }
}
