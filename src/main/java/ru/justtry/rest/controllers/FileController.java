package ru.justtry.rest.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.justtry.database.Database;
import ru.justtry.fileprocessing.ImageService;
import ru.justtry.shared.Utils;

@CrossOrigin(maxAge = 3600, origins = "*")
@RestController
@RequestMapping("/rest/file")
@Slf4j
@RequiredArgsConstructor
public class FileController
{
    private final Database database;
    private final ImageService imageService;
    private final Utils utils;


    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> upload(@RequestParam MultipartFile file)
    {
        HttpHeaders headers = new HttpHeaders();

        try
        {
            if (file.isEmpty())
                throw new IllegalArgumentException("Empty file");

            String id = database.saveFile(file);
            imageService.createResizedCopies(id, 50, 100, 200);

            return new ResponseEntity<>(id, headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            // If we already have file, we should extract his md5 from error message and find id
            // E11000 duplicate key error collection: notes.files.files index: md5_1 dup key:
            // { md5: "daa94cc58fdc27b5762aa610ebd4e593" }
            if (e.getMessage().contains("duplicate key error"))
            {
                log.info("Trying to getById id by md5");
                String message = e.getMessage();
                String md5 = message.substring(message.indexOf("\"") + 1, message.lastIndexOf("\""));
                return new ResponseEntity<>(database.getFileId(md5), headers, HttpStatus.OK);
            }
            else
            {
                log.error(e.toString());
            }

            return utils.getResponseForError(headers, e);
        }
    }


    @PostMapping(path = "/image/{originalId}/{size}", consumes = APPLICATION_JSON_VALUE,
        produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> downloadImage(@PathVariable String originalId, @PathVariable int size)
            throws Exception
    {
        return new ResponseEntity<>(imageService.getImage(originalId, size).getImage(), new HttpHeaders(), HttpStatus.OK);
    }


    @GetMapping(path = "/{id}/content", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> download(@PathVariable String id)
    {
        HttpHeaders headers = new HttpHeaders();
        Document metadata = database.getMetadata(id);
        headers.setContentLength(Long.parseLong(metadata.get("size").toString()));
        return new ResponseEntity<>(database.getFileStream(id), headers, HttpStatus.OK);
    }


    @GetMapping(path = "/metadata/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getMetadata(@PathVariable String id)
    {
        return new ResponseEntity<>(database.getMetadata(id), new HttpHeaders(), HttpStatus.OK);
    }


    @PostMapping(path = "/metadata", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getMultipleMetadata(@RequestBody String json) throws JsonProcessingException
    {
        List<String> identifiers = new ObjectMapper().readValue(json, ArrayList.class);
        return new ResponseEntity<>(database.getMetadata(identifiers), new HttpHeaders(), HttpStatus.OK);
    }
}
