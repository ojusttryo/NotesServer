package ru.justtry.rest.controllers;

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
import ru.justtry.mappers.FolderMapper;
import ru.justtry.notes.NoteFolder;
import ru.justtry.validation.FolderValidator;

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
        //return database.saveDocument(getCollectionName(entity), this, folder);
        return null;
    }


    @PutMapping(value = "/{entity}", consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable(value = ENTITY) String entity,
                       @RequestBody NoteFolder folder)
    {
        //database.updateDocument(getCollectionName(entity), this, folder);
    }



    @Override
    public String getCollectionName(String entity)
    {
        return entity + ".folders";
    }
}
