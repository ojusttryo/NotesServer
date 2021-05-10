package ru.justtry.shared;

import java.time.Instant;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import ru.justtry.rest.errors.RestError;

@Component
public class Utils
{
    public boolean equals(Double x, Double y, Double threshold)
    {
        return ((x - y) < threshold);
    }


    public ResponseEntity<Object> getResponseForError(HttpHeaders headers, Exception e)
    {
        return new ResponseEntity<>(new RestError(e.getMessage()), headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    public long getTimeInMs()
    {
        return Instant.now().getEpochSecond() * 1000;
    }
}
