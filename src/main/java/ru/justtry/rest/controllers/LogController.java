package ru.justtry.rest.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ru.justtry.database.Database;
import ru.justtry.database.LogRecord;

@RestController
@CrossOrigin(maxAge = 3600)
@RequiredArgsConstructor
public class LogController
{
    protected final Database database;


    @GetMapping("/rest/log/{count}")
    public ResponseEntity<LogRecord[]> get(@PathVariable int count)
    {
        return new ResponseEntity<>(database.getLog(count), new HttpHeaders(), HttpStatus.OK);
    }

    @GetMapping("/rest/log")
    public ResponseEntity<LogRecord[]> get()
    {
        return new ResponseEntity<>(database.getLog(), new HttpHeaders(), HttpStatus.OK);
    }

}
