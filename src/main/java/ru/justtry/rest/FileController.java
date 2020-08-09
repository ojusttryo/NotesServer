package ru.justtry.rest;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    final static Logger logger = LogManager.getLogger(FileController.class);

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
            logger.error(e);
            // If we already have file, we should extract his md5 from error message and find id
            // E11000 duplicate key error collection: notes.files.files index: md5_1 dup key:
            // { md5: "daa94cc58fdc27b5762aa610ebd4e593" }
            if (e.getMessage().contains("duplicate key error"))
            {
                try
                {
                    logger.info("Trying to get id by md5");
                    String message = e.getMessage();
                    // Regex [a-f0-9]{32} in some reason doesn't want to work
                    String md5 = message.substring(message.indexOf("\"") + 1, message.lastIndexOf("\""));
                    return new ResponseEntity<>(database.getFileId(md5), headers, HttpStatus.OK);
                }
                catch (Exception x)
                {
                    logger.error(x);
                    return new ResponseEntity<>(new RestError(x.getMessage()), headers,
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
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
            logger.error(e);
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
            logger.error(e);
            return new ResponseEntity<>(new RestError(e.getMessage()), headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
