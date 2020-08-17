package ru.justtry.database;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.lt;
import static ru.justtry.shared.AttributeConstants.ATTRIBUTES_COLLECTION;
import static ru.justtry.shared.Constants.MONGO_ID;
import static ru.justtry.shared.Constants.NAME;
import static ru.justtry.shared.EntityConstants.COLLECTION;
import static ru.justtry.shared.EntityConstants.ENTITIES_COLLECTION;
import static ru.justtry.shared.ScaledImageConstants.ORIGINAL_ID;
import static ru.justtry.shared.ScaledImageConstants.SIZE;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ReadConcern;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import ru.justtry.mappers.LogMapper;
import ru.justtry.rest.AttributesController;
import ru.justtry.rest.EntitiesController;

public class Database
{
    private static final Logger logger = LogManager.getLogger(Database.class);
    private static final String LOG_COLLECTION = "log";
    private static final String FILES_COLLECTION = "files";
    private static final String NOTES_FILES_COLLECTION = "notes.files";
    private static final String IMAGES_COLLECTION = "notes.images";

    private MongoDatabase database;
    @Autowired
    private LogMapper logMapper;
    @Autowired
    private AttributesController attributesController;
    @Autowired
    private EntitiesController entitiesController;


    public Database()
    {

    }


    public void init(String host, Integer port, String name, String user, String password)
    {
        ServerAddress address = new ServerAddress(host, port);
        MongoClientOptions options = MongoClientOptions
                .builder()
                .writeConcern(WriteConcern.ACKNOWLEDGED)
                .readConcern(ReadConcern.LOCAL)
                .build();
        MongoClient mongo = new MongoClient(address, options);
        database = mongo.getDatabase(name);

        MongoCollection<Document> filesCollection = database.getCollection(FILES_COLLECTION + ".files");
        filesCollection.createIndex(new BasicDBObject("md5", 1), new IndexOptions().unique(true));

        MongoCollection<Document> notesFilesCollection = database.getCollection(NOTES_FILES_COLLECTION);
        BasicDBObject notesFilesRestriction = new BasicDBObject()
                .append("noteId", 1)
                .append("attributeName", 1)
                .append("fileId", 1);
        notesFilesCollection.createIndex(notesFilesRestriction, new IndexOptions().unique(true));

        logger.info("Database created");
    }


    public String saveDocument(String collectionName, Document document)
    {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.insertOne(document);
        return ((ObjectId)document.get(MONGO_ID)).toString();
    }


    public void updateDocument(String collectionName, Document document)
    {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        UpdateResult result = collection.updateOne(
                eq(MONGO_ID, document.get(MONGO_ID)), new Document("$set", document));
        if (result.getMatchedCount() != 1)
            throw new RuntimeException("There are no object with id " + document.get(MONGO_ID));
    }

    public void deleteDocument(String collectionName, String id)
    {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        DeleteResult result = collection.deleteOne(eq(MONGO_ID, new ObjectId(id)));
        if (result.getDeletedCount() != 1)
            throw new RuntimeException(String.format("Cannot delete %s: document not found", id));
    }

    public long dropCollection(String collectionName)
    {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        long count = collection.estimatedDocumentCount();
        collection.drop();
        return count;
    }

    public Document getDocument(String collectionName, String id)
    {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        FindIterable<Document> iterable = collection.find(eq(MONGO_ID, new ObjectId(id))).limit(1);
        try (MongoCursor<Document> cursor = iterable.iterator())
        {
            return cursor.hasNext() ? cursor.next() : null;
        }
    }

    public List<Document> getDocuments(String collectionName, List<String> ids)
    {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        FindIterable<Document> iterable = find(collection, ids);
        List<Document> documents = new ArrayList<>();
        try (MongoCursor<Document> cursor = iterable.iterator())
        {
            while (cursor.hasNext())
                documents.add(cursor.next());
        }
        return documents;
    }


    public Document getAttribute(String name)
    {
        MongoCollection<Document> collection = database.getCollection(ATTRIBUTES_COLLECTION);
        FindIterable<Document> iterable = collection.find(eq(NAME, name)).limit(1);
        try (MongoCursor<Document> cursor = iterable.iterator())
        {
            return cursor.hasNext() ? cursor.next() : null;
        }
    }


    public Document getEntity(String entityCollection)
    {
        MongoCollection<Document> collection = database.getCollection(entitiesController.getCollectionName());
        FindIterable<Document> iterable = collection.find(eq(COLLECTION, entityCollection)).limit(1);
        try (MongoCursor<Document> cursor = iterable.iterator())
        {
            return cursor.hasNext() ? cursor.next() : null;
        }
    }

    public boolean isEntityExist(String entityCollection)
    {
        MongoCollection<Document> collection = database.getCollection(ENTITIES_COLLECTION);
        FindIterable<Document> iterable = collection.find(eq(COLLECTION, entityCollection)).limit(1);
        try (MongoCursor<Document> cursor = iterable.iterator())
        {
            return cursor.hasNext();
        }
    }


    private FindIterable<Document> find(MongoCollection<Document> collection, List<String> ids)
    {
        if (ids == null || ids.size() == 0)
        {
            return collection.find();
        }
        else
        {
            List<ObjectId> identifiers = new ArrayList<>(ids.size());
            for (String id : ids)
                identifiers.add(new ObjectId(id));

            return collection.find(in(MONGO_ID, identifiers));
        }
    }


    public Object[] getLog()
    {
        MongoCollection<Document> collection = database.getCollection(LOG_COLLECTION);

        FindIterable<Document> iterable = collection.find();
        List<LogRecord> result = new ArrayList<>();
        try (MongoCursor<Document> cursor = iterable.iterator())
        {
            while (cursor.hasNext())
            {
                Document document = cursor.next();
                result.add((LogRecord)logMapper.getObject(document));
            }
        }
        return result.toArray();
    }


    public Object[] getLog(int count)
    {
        MongoCollection<Document> collection = database.getCollection(LOG_COLLECTION);
        FindIterable<Document> iterable = collection.find().sort(new Document(MONGO_ID, -1)).limit(count);
        List<LogRecord> result = new ArrayList<>();
        try (MongoCursor<Document> cursor = iterable.iterator())
        {
            while (cursor.hasNext())
            {
                Document document = cursor.next();
                result.add((LogRecord)logMapper.getObject(document));
            }
        }
        return result.toArray();
    }


    public void saveLog(String collectionName, String operation, String id, Object before, Object after)
    {
        LogRecord logRecord = new LogRecord(collectionName, operation, id, before, after);
        Document document = logMapper.getDocument(logRecord);

        MongoCollection<Document> collection = database.getCollection(LOG_COLLECTION);
        collection.insertOne(document);
    }


    public String saveFile(MultipartFile file) throws IOException
    {
        GridFSBucket bucket = GridFSBuckets.create(database, FILES_COLLECTION);

        Document metaData = new Document();
        metaData.put("contentType", file.getContentType());
        metaData.put("title", file.getOriginalFilename());
        metaData.put("size", file.getSize());
        metaData.put("uploaded", Instant.now().getEpochSecond());

        GridFSUploadOptions options = new GridFSUploadOptions().metadata(metaData);

        ObjectId id = bucket.uploadFromStream(file.getOriginalFilename(), file.getInputStream(), options);
        return id.toString();
    }


    public void linkFilesAndNote(String noteId, String attributeName, Collection<String> fileIds)
    {
        MongoCollection<Document> collection = database.getCollection(NOTES_FILES_COLLECTION);

        for (String fileId : fileIds)
        {
            try
            {
                Document document = new Document()
                        .append("noteId", noteId)
                        .append("attributeName", attributeName)
                        .append("fileId", fileId);
                collection.insertOne(document);
            }
            catch (Exception e)
            {
                // Maybe we connect the same note attribute to the same file
                logger.error(e);
            }
        }
    }


    public void unlinkFilesAndNote(String noteId, String attributeName)
    {
        MongoCollection<Document> collection = database.getCollection(NOTES_FILES_COLLECTION);
        Document document = new Document()
                .append("noteId", noteId)
                .append("attributeName", attributeName);
        collection.deleteOne(document);
    }


    public void unlinkFilesAndNote(String noteId, String attributeName, Collection<String> fileIds)
    {
        if (fileIds != null && fileIds.size() > 0)
        {
            MongoCollection<Document> collection = database.getCollection(NOTES_FILES_COLLECTION);
            Document document = new Document()
                    .append("noteId", noteId)
                    .append("attributeName", attributeName);
            collection.deleteOne(and(document, in("fileId", fileIds)));
        }
    }


    public String getFileId(String md5)
    {
        GridFSBucket bucket = GridFSBuckets.create(database, FILES_COLLECTION);

        GridFSFindIterable iterable = bucket.find(eq("md5", md5)).limit(1);
        GridFSFile file = iterable.first();

        if (file == null)
            throw new NoSuchElementException("Cannot find file with md5 " + md5);

        return file.getObjectId().toString();
    }


    public int removeFilesOlderThan(long time)
    {
        MongoCollection notesFiles = database.getCollection(NOTES_FILES_COLLECTION);
        Set<String> usedIds = (HashSet<String>)notesFiles
                .distinct("fileId", String.class).into(new HashSet<String>());

        MongoCollection imagesCollection = database.getCollection(IMAGES_COLLECTION);

        GridFSBucket bucket = GridFSBuckets.create(database, FILES_COLLECTION);

        GridFSFindIterable iterable = bucket.find(lt("metadata.uploaded", time));
        try (MongoCursor cursor = iterable.iterator())
        {
            int count = 0;
            while (cursor.hasNext())
            {
                GridFSFile file = (GridFSFile)cursor.next();
                if (!usedIds.contains(file.getObjectId().toString()))
                {
                    // Removing scaled duplicates of images
                    String contentType = (String)file.getMetadata().get("contentType");
                    if (contentType != null && contentType.startsWith("image"))
                    {
                        Document document = new Document().append(ORIGINAL_ID, file.getObjectId().toString());
                        imagesCollection.deleteMany(document);
                    }

                    // Removing image itself
                    bucket.delete(file.getObjectId());
                    count++;
                }
            }
            return count;
        }
    }


    public GridFsResource getFile(String id)
    {
        ObjectId fileId = new ObjectId(id);

        GridFSBucket bucket = GridFSBuckets.create(database, FILES_COLLECTION);

        GridFSFindIterable iterable = bucket.find(eq(MONGO_ID, fileId)).limit(1);
        GridFSFile file = iterable.first();


        return new GridFsResource(file, bucket.openDownloadStream(file.getObjectId()));

        //return bucket.openDownloadStream(fileId);


/*
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        Video video = new Video();
        video.setTitle(file.getMetadata().get("title").toString());
        video.setStream(operations.getResource(file).getInputStream());

        FileOutputStream streamToDownloadTo = new FileOutputStream("/tmp/mongodb-tutorial.pdf");
        gridFSBucket.downloadToStream(fileId, streamToDownloadTo);
        streamToDownloadTo.close();
        System.out.println(streamToDownloadTo.toString());*/
    }


    public Object getMetadata(String id)
    {
        ObjectId fileId = new ObjectId(id);

        GridFSBucket bucket = GridFSBuckets.create(database, FILES_COLLECTION);

        GridFSFindIterable iterable = bucket.find(eq(MONGO_ID, fileId)).limit(1);
        GridFSFile file = iterable.first();

        Document metadata = file.getMetadata();
        metadata.put("id", id);
        return metadata;
    }


    public List<Document> getMetadata(Collection<String> ids)
    {
        List<ObjectId> fileIds = ids.stream().map(ObjectId::new).collect(Collectors.toList());

        GridFSBucket bucket = GridFSBuckets.create(database, FILES_COLLECTION);

        GridFSFindIterable iterable = bucket.find(in(MONGO_ID, fileIds));
        List<Document> metadata = new ArrayList<>();
        try (MongoCursor<GridFSFile> iterator = iterable.iterator())
        {
            while (iterator.hasNext())
            {
                GridFSFile file = iterator.next();
                Document meta = file.getMetadata();
                meta.put("id", file.getObjectId().toString());
                metadata.add(meta);
            }
        }

        return metadata;
    }


    public String saveImage(Document image)
    {
        MongoCollection<Document> collection = database.getCollection(IMAGES_COLLECTION);
        collection.insertOne(image);
        return ((ObjectId)image.get(MONGO_ID)).toString();
    }

    public List<Document> getImages(List<String> identifiers, int size)
    {
        MongoCollection<Document> collection = database.getCollection(IMAGES_COLLECTION);
        FindIterable<Document> iterable = collection.find(and(in(ORIGINAL_ID, identifiers), eq(SIZE, size)));
        List<Document> images = new ArrayList<>();
        try (MongoCursor<Document> iterator = iterable.iterator())
        {
            while (iterator.hasNext())
                images.add(iterator.next());

            return images;
        }
    }

    public Document getImage(String identifier, int size)
    {
        MongoCollection<Document> collection = database.getCollection(IMAGES_COLLECTION);
        FindIterable<Document> iterable = collection.find(and(eq(ORIGINAL_ID, identifier), eq(SIZE, size)));
        try (MongoCursor<Document> iterator = iterable.iterator())
        {
            return (iterator.hasNext()) ? iterator.next() : null;
        }
    }
}


/**
 * Expected hierarchy of data stored in MongoDB:
 *
 *         Versions: [
 *             notes:
 *             serials:
 *             movies
 *             animeSerials
 *             animeMovies
 *             ...
 *         ]
 *
 *         Serials:
 *             lastUpdate:
 *             data: [
 *             {
 *                 id
 *                 saved
 *                 updated
 *                 name: [
 *                     EN:
 *                     RU:
 *                 ]
 *                 description: [
 *                     EN:
 *                     RU:
 *                 ]
 *                 plot: [
 *                     EN:
 *                     RU:
 *                 ]
 *                 seasonsCount
 *                 //seriesCount - absent, because there are serials with dynamic series count
 *                 releaseDate
 *                 rating: [
 *                     imdb
 *                     kinopoisk
 *                     lostfilm
 *                     ...
 *                 ]
 *                 type: [
 *                     Space
 *                     Time travel
 *                     ...
 *                 }
 *                 genre: [
 *                     drama,
 *                     fantastics
 *                 }
 *                 photos: [
 *                     // refs or photos from server
 *                 ]
 *
 *                 author: Netflix
 *                 country: USA
 *             }
 *             ...
 *         }
 *
 *         Movies: [
 *             ...
 *         ]
 *
 *         AnimeSerials: [
 *             ...
 *         ]
 *
 *         AnimeMovies: [
 *             ...
 *         ]
 *
 *         Literature: [
 *             ...
 *         ]
 *
 *         Users: [
 *             {
 *                 id: 1
 *                 name: John Wick
 *                 passowrd: somehash
 *                 email: email@somemail.com
 *                 phone: +7 978 ...
 *                 birthdate: 16.07.1990
 *                 address:
 *                 {
 *                     country: USA,
 *                     city: New York
 *                     ...
 *                 }
 *
 *                 registered: timestamp
 *                 lastVisited: timestamp
 *                 lastActivity: timestamp
 *
 *                 // Add photo list to almost all notes and reference to this table.
 *                 photos: [
 *                     {
 *                         id
 *                         name
 *                         date
 *                         author
 *                         groups: [ Travel, Naumen ]
 *                         people: []
 *                         location
 *                         state
 *                         comment
 *                     }
 *                 ]
 *
 *
 *                 GeneralAffairs: [
 *                     {
 *                         id: 1
 *                         name: Write book,
 *                         description: Write book about psychology,
 *                         executeDate: date,
 *                         state: State.
 *                         comment: Some comment
 *                         added: date
 *                         lastUpdate: date
 *
 *                         priorityLevel: 1-10
 *                         price: total price for all inner affairs
 *
 *                         // Inner affairs. I.E. Make good health: teeth: clean, remove
 *                         Affairs: [
 *                             ...
 *                             Affairs: [ ... ]
 *                         ]
 *                     }
 *                     ...
 *                 ]
 *
 *                 AnimeFilms: [
 *                     {
 *                         id: 1
 *                         name: Name,
 *                         year: Year,
 *                         state: State,
 *                         comment: Some comment
 *                         ownRatio:
 *                     }
 *                     ...
 *                 ]
 *
 *                 AnimeSerials: [
 *                     {
 *                         id: 1
 *                         name
 *                         season
 *                         episode
 *                         state
 *                         comment
 *                         ownRatio
 *                     }
 *                     ...
 *                 ]
 *
 *                 Bookmarks: [
 *                     {
 *                         id
 *                         name
 *                         url
 *                         login
 *                         password
 *                         email
 *                         state
 *                         comment
 *                         file: (archieve of site)
 *
 *                         // inner bookmarks
 *                         bookmarks: [
 *                             ...
 *                             bookmarks: [ ... ]
 *                         ]
 *                     }
 *                     ...
 *                 ]
 *
 *                 Desires: [
 *                     {
 *                         id
 *                         name
 *                         description
 *                         state
 *                         comment
 *                         priorityLevel: 1-10
 *                         price: total price for all inner desires
 *
 *                         // inner desires. For example, bicycle, and the bag/ring/wheel for it.
 *                         desires: [
 *                             ...
 *                             desires: [ ... ]
 *                         ]
 *                     }
 *                     ...
 *                 ]
 *
 *                 Movies: [
 *                     {
 *                         id
 *                         name
 *                         year
 *                         state
 *                         comment
 *                         ownRatio
 *                     }
 *                     ...
 *                 ]
 *
 *                 Games: [
 *                     {
 *                         id
 *                         name
 *                         version
 *                         genre
 *                         type (singleplayer/multiplayer/mixed)
 *                         link
 *                         login
 *                         password
 *                         email
 *                         state
 *                         comment
 *                         ownRatio
 *                     }
 *                     ...
 *                 }
 *
 *                 Literature: [
 *                     {
 *                         id
 *                         name
 *                         author
 *                         genre
 *                         universe (S.T.A.L.K.E.R, Metro 2033, etc)
 *                         series (some series in universe or without it)
 *                         audio: (true if audio, else - text)
 *                         volume
 *                         currentChapter
 *                         currentPage
 *                         pagesCount
 *                         year
 *                         state
 *                         comment
 *                         ownRatio
 *                     }
 *                     ...
 *                 ]
 *
 *                 Meal: [
 *                     {
 *                         id
 *                         name
 *                         ingredients
 *                         recipe
 *                         state
 *                         comment
 *                     }
 *                     ...
 *                 ]
 *
 *                 Performances: [
 *                     {
 *                         id
 *                         name
 *                         year
 *                         state
 *                         comment
 *                         ownRaio
 *                     }
 *                     ...
 *                 ]
 *
 *                 People: [
 *                     {
 *                         id
 *                         name
 *                         nickname
 *                         sex
 *                         birthdate
 *                         address
 *                         contacts:
 *                         {
 *                             phone (always)
 *                             telegram (custom)
 *                             viber (custom)
 *                             ...
 *                         }
 *                         groups:
 *                         {
 *                             id: 1
 *                             id: 2
 *                             ...
 *                         }
 *                         state
 *                         comment
 *                     }
 *                     ...
 *                 ]
 *
 *                 PropleGroups: [
 *                     { id: 1, name: Tavrida },
 *                     { id: 2, name: Naumen },
 *                     ...
 *                 ]
 *
 *                 Programs: [
 *                     {
 *                         id
 *                         name
 *                         version
 *                         link (from Bookmarks or new)
 *                         login
 *                         password
 *                         email
 *                         state
 *                         comment
 *                     }
 *                     ...
 *                 ]
 *
 *                 DailyAffairs: [
 *                     {
 *                         id
 *                         date: (for every day)
 *                         name
 *                         description
 *                         state
 *                         comment
 *                         priorityLevel: 1-10
 *                         price: (sum from all inner)
 *                         repeat: (every day, one time, etc)
 *
 *                         // Inner affairs. Example: morning routine: teeth, toilet, food, etc.
 *                         DailyAffairs: [
 *                             ...
 *                             DailyAffairs: [ ... ]
 *                         ]
 *                     }
 *                     ...
 *                 ]
 *
 *                 Serials: [
 *                     {
 *                         id
 *                         name
 *                         season
 *                         episode
 *                         state
 *                         comment
 *                         ownRatio
 *                     }
 *                     ...
 *                 ]
 *
 *                 TVShows: [
 *                     {
 *                         id
 *                         name
 *                         season
 *                         episode
 *                         state
 *                         comment
 *                         added
 *                         lastUpdated
 *                         ownRatio
 *                     }
 *                     ...
 *                 ]
 *
 *                 Concerts: [
 *                     {
 *                         id
 *                         name
 *                         description
 *                         visited: boolean
 *                         location
 *                         date
 *                         state
 *                         comment
 *                         added
 *                         lastUpdated
 *                         ownRatio
 *                     }
 *                 ]
 *
 *                 // Some walks by city, etc
 *                 Walks: [
 *                     {
 *                         id
 *                         name
 *                         description
 *                         location
 *                         date
 *                         state
 *                         comment
 *                         added
 *                         lastUpdated
 *                         photos: [
 *                             id: 1
 *                             id: 2
 *                         ]
 *                     }
 *                 ]
 *
 *                 Travels: [
 *                     {
 *                         id
 *                         name
 *                         description
 *                         locations: [
 *                             { id: 1, location: name/point, date, photos }
 *                             ...
 *                         ]
 *                         date
 *                         state
 *                         comment
 *                         added
 *                         lastUpdated
 *                         sharedPhotos: [
 *                             id: 1
 *                             id: 2
 *                         ]
 *                     }
 *                     ...
 *                 ]
 *
 *                 Minds: [
 *                     {
 *                         // Просто мысли человека. Что-то полезное, интересное, что-то что хочется запомнить и т.п.
 *                         // Сюда же можно вносить новые идеи или лайфхаки, полезные для человека.
 *                     }
 *                 ]
 *
 *                 // goals in life
 *                 Goals: [
 *                     priorityLeveL: 1-10
 *                     date
 *                     type: single (in that date), every day (until the date come), etc
 *
 *                     Goals: [
 *                         ...
 *                     ]
 *                     state
 *                     comment
 *                 ]
 *
 *                 Projects: [
 *                     priorityLevel: 1-10
 *                     state
 *                     comment
 *                     author
 *                     whatAffects: (what change if you do project)
 *                     relation: (with what projects or something else is this project connected)
 *                     benefit:
 *                     harm:
 *                     // sum from tasks
 *                     costs: [
 *                         money:
 *                         time
 *                     ]
 *
 *                     // some tasks to do project
 *                     Tasks: [
 *                         priorityLevel: 1-10
 *                         date
 *                         name
 *                         costs: [
 *                             money
 *                             time
 *                         ]
 *                         state
 *                         comment
 *                         ...
 *                     ]
 *                 ]
 *             }
 *         ]
 *
 *
 */

