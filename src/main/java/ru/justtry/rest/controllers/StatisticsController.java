package ru.justtry.rest.controllers;

import static ru.justtry.database.Database.IMAGES_COLLECTION;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.justtry.database.Database;
import ru.justtry.database.info.CollectionInfo;
import ru.justtry.database.info.DatabaseInfo;
import ru.justtry.database.info.FilesInfo;
import ru.justtry.metainfo.Entity;
import ru.justtry.metainfo.EntityService;
import ru.justtry.notes.NoteService;

@CrossOrigin(maxAge = 3600, origins = "*")
@RestController
@RequestMapping("/rest/info")
@RequiredArgsConstructor
@Slf4j
public class StatisticsController
{
    private final Database database;
    private final EntityService entityService;
    private final NoteService noteService;


    @GetMapping("/notes")
    public ResponseEntity<Object[]> getNotesInfo()
    {
        List<CollectionInfo> allCollectionsInfo = new ArrayList<>();
        Entity[] entities = entityService.getAll();
        for (Entity entity : entities)
        {
            CollectionInfo collectionInfo = new CollectionInfo();
            collectionInfo.setEntityName(entity.getName());
            collectionInfo.setEntityTitle(entity.getTitle());
            String collectionName = noteService.getCollectionName(entity.getName());
            Document infoDocument = database.getCollectionInfo(collectionName);
            collectionInfo.setCount(infoDocument.get("count"));
            collectionInfo.setSize(infoDocument.get("size"));
            collectionInfo.setStorageSize(infoDocument.get("storageSize"));
            allCollectionsInfo.add(collectionInfo);
        }

        return new ResponseEntity<>(allCollectionsInfo.toArray(), new HttpHeaders(), HttpStatus.OK);
    }


    @GetMapping("/files")
    public ResponseEntity<Object> getFilesInfo()
    {
        List<FilesInfo> filesInfo = new ArrayList<>();
        List<Document> infoDocuments = database.getFilesInfo();
        for (Document document : infoDocuments)
        {
            FilesInfo fileInfo = new FilesInfo();
            fileInfo.setContentType(document.get("contentType").toString().replaceAll("[\\[\\]]+", ""));
            fileInfo.setCount(document.get("count"));
            fileInfo.setSize(document.get("size"));
            filesInfo.add(fileInfo);
        }

        return new ResponseEntity<>(filesInfo, new HttpHeaders(), HttpStatus.OK);
    }


    @GetMapping("/db")
    public ResponseEntity<Object> getDatabaseInfo()
    {
        Document infoDocument = database.getDatabaseInfo();

        DatabaseInfo databaseInfo = new DatabaseInfo();
        databaseInfo.setName(infoDocument.get("db").toString());
        databaseInfo.setCollections(infoDocument.get("collections"));
        databaseInfo.setDataSize(infoDocument.get("dataSize"));
        databaseInfo.setStorageSize(infoDocument.get("storageSize"));

        return new ResponseEntity<>(databaseInfo, new HttpHeaders(), HttpStatus.OK);
    }


    @GetMapping("/icons")
    public ResponseEntity<Object> getIconsInfo()
    {
        Document infoDocument = database.getCollectionInfo(IMAGES_COLLECTION);

        FilesInfo iconsInfo = new FilesInfo();
        iconsInfo.setCount(infoDocument.get("count"));
        iconsInfo.setSize(infoDocument.get("size"));
        iconsInfo.setContentType("icons for all images");

        return new ResponseEntity<>(iconsInfo, new HttpHeaders(), HttpStatus.OK);
    }

}
