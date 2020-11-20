package ru.justtry.rest;

import static ru.justtry.shared.Constants.ID;
import static ru.justtry.shared.NoteConstants.ENTITY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import ru.justtry.notes.NoteService;
import ru.justtry.shared.Identifiable;

@CrossOrigin(maxAge = 3600)
public abstract class ObjectsController
{
    abstract String getCollectionName(String entity);

    @Autowired
    private NoteService noteService;


    @GetMapping("/{entity}/{id}")
    @ResponseBody
    public Identifiable get(@PathVariable(value = ENTITY) String entity,
                      @PathVariable(value = ID) String id)
    {
        return noteService.getRegular(getCollectionName(entity), id);
    }


    @GetMapping("/{entity}")
    @ResponseBody
    public ResponseEntity<Identifiable[]> getAll(@PathVariable(value = ENTITY) String entity)
    {
        HttpHeaders headers = new HttpHeaders();
        Identifiable[] objects = noteService.getRegular(entity);
        return new ResponseEntity<>(objects, headers, HttpStatus.OK);
    }
}
