package ru.justtry.rest;

import static ru.justtry.shared.Constants.ID;
import static ru.justtry.shared.NoteConstants.ENTITY;

import javax.inject.Inject;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ru.justtry.database.Database;

@RestController
@CrossOrigin(maxAge = 3600)
public class LogController
{
    @Inject
    protected Database database;

    @GetMapping("/rest/log/{count}")
    @ResponseBody
    public ResponseEntity<Object[]> get(@PathVariable(value = "count") int count)
    {
        return new ResponseEntity<>(database.getLog(count), new HttpHeaders(), HttpStatus.OK);
    }


    @GetMapping("/rest/log")
    @ResponseBody
    public ResponseEntity<Object[]> get()
    {
        return new ResponseEntity<>(database.getLog(), new HttpHeaders(), HttpStatus.OK);
    }
}
