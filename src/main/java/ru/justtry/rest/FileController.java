package ru.justtry.rest;

import java.awt.image.BufferedImage;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.justtry.database.Database;
import ru.justtry.fileprocessing.ImageEditor;
import ru.justtry.fileprocessing.ScaledImage;
import ru.justtry.mappers.ScaledImageMapper;
import ru.justtry.shared.Identifiable;
import ru.justtry.shared.RestError;
import ru.justtry.shared.Utils;

@CrossOrigin(maxAge = 3600, origins = "*")
@RestController
@RequestMapping("/rest/file")
public class FileController
{
    final static Logger logger = LogManager.getLogger(FileController.class);

    @Autowired
    private Database database;
    @Autowired
    private ImageEditor imageEditor;
    @Autowired
    private ScaledImageMapper imageMapper;
    @Autowired
    private Utils utils;

    @PostMapping(produces = "application/json;charset=UTF-8", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<Object> upload(@RequestParam MultipartFile file)
    {
        HttpHeaders headers = new HttpHeaders();

        try
        {
            if (file.isEmpty())
                throw new IllegalArgumentException("Empty file");

            String id = database.saveFile(file);

            String contentType = file.getContentType();
            if (contentType != null && contentType.startsWith("image"))
            {
                String extension = contentType.replace("image/", "");
                GridFsResource resource = database.getFile(id);
                BufferedImage image = ImageIO.read(resource.getInputStream());

                saveResizedCopy(image, 50, id, extension);
                saveResizedCopy(image, 100, id, extension);
                saveResizedCopy(image, 200, id, extension);
            }

            return new ResponseEntity<>(id, headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            // If we already have file, we should extract his md5 from error message and find id
            // E11000 duplicate key error collection: notes.files.files index: md5_1 dup key:
            // { md5: "daa94cc58fdc27b5762aa610ebd4e593" }
            if (e.getMessage().contains("duplicate key error"))
            {
                try
                {
                    logger.info("Trying to getById id by md5");
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
            else
            {
                logger.error(e);
            }

            return utils.getResponseForError(headers, e);
        }
    }


    /**
     * Returns a list if images with binary data inside encoded with Base64
     */
    @PostMapping(path = "/images/{size}", consumes = "application/json;charset=UTF-8",
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<Object> downloadImages(@RequestBody List<String> identifiers, @PathVariable int size)
    {
        HttpHeaders headers = new HttpHeaders();

        try
        {
            List<Document> imageDocs = database.getImages(identifiers, size);
            Identifiable[] imageObjects = imageMapper.getObjects(imageDocs);
            return new ResponseEntity<>(imageObjects, headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            logger.error(e);
            return utils.getResponseForError(headers, e);
        }
    }


    @PostMapping(path = "/image/{originalId}/{size}", consumes = "application/json;charset=UTF-8",
        produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<Object> downloadImage(@PathVariable String originalId, @PathVariable int size)
    {
        HttpHeaders headers = new HttpHeaders();

        try
        {
            Document imageDoc = database.getImage(originalId, size);
            if (imageDoc == null)
                throw new NoSuchFileException(
                        String.format("Image with id=%s and size=%d not found", originalId, size));
            ScaledImage imageObject = (ScaledImage)imageMapper.getObject(imageDoc);
            return new ResponseEntity<>(imageObject.getImage(), headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            logger.error(e);
            return utils.getResponseForError(headers, e);
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
            return utils.getResponseForError(headers, e);
        }
    }


    @GetMapping(path = "/metadata/{id}", consumes = "application/json;charset=UTF-8",
            produces = "application/json;charset=UTF-8")
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
            return utils.getResponseForError(headers, e);
        }
    }


    @PostMapping(path = "/metadata", consumes = "application/json;charset=UTF-8",
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<Object> getMultipleMetadata(@RequestBody String json)
    {
        HttpHeaders headers = new HttpHeaders();

        try
        {
            List<String> identifiers = new ObjectMapper().readValue(json, ArrayList.class);
            return new ResponseEntity<>(database.getMetadata(identifiers), headers, HttpStatus.OK);
        }
        catch (Exception e)
        {
            logger.error(e);
            return utils.getResponseForError(headers, e);
        }
    }


    private String saveResizedCopy(BufferedImage image, int size, String id, String extension) throws Exception
    {
        BufferedImage bufferedImage = imageEditor.resize(image, size, size);
        ScaledImage scaledImage = new ScaledImage(id, size, imageEditor.convert(bufferedImage, extension));
        Document documentToSave = imageMapper.getDocument(scaledImage);
        return database.saveImage(documentToSave);
    }
}
