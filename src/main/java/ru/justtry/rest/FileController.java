package ru.justtry.rest;

import javax.inject.Inject;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ru.justtry.database.Database;
import ru.justtry.shared.RestError;

@CrossOrigin(maxAge = 3600, origins = "*")
@RestController
@RequestMapping("/rest/file")
public class FileController
{
    @Inject
    protected Database database;

    @PostMapping(produces = "application/json;charset=UTF-8", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<Object> upload(@RequestParam MultipartFile file)
    {
        HttpHeaders headers = new HttpHeaders();

        try
        {
            if (file.isEmpty())
                throw new IllegalArgumentException("Empty file");

            return new ResponseEntity<>(database.saveFile(file), headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(new RestError(e.getMessage()), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping(path = "/{id}/content", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<Object> download(@PathVariable String id)
    {
        HttpHeaders headers = new HttpHeaders();

        try
        {
            return new ResponseEntity<>(database.getFile(id), headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(new RestError(e.getMessage()), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping(path = "/{id}/metadata", consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<Object> getMetadata(@PathVariable String id)
    {
        HttpHeaders headers = new HttpHeaders();

        try
        {
            return new ResponseEntity<>(database.getMetadata(id), headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(new RestError(e.getMessage()), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
