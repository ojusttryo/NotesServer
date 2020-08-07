package ru.justtry.database;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static ru.justtry.shared.Constants.MONGO_ID;
import static ru.justtry.shared.Constants.NAME;
import static ru.justtry.shared.EntityConstants.COLLECTION;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
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
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import ru.justtry.mappers.LogMapper;
import ru.justtry.mappers.Mapper;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.Entity;
import ru.justtry.rest.AttributesController;
import ru.justtry.rest.EntitiesController;
import ru.justtry.validation.Validator;

//@Component
//@PropertySource("classpath:application.properties")
//@Component
//@Scope("singleton")
//@Named("database")
public class Database
{
    final static Logger logger = LogManager.getLogger(Database.class);
    final static String LOG_COLLECTION = "log";
    final static String FILES_COLLECTION = "files";

    private MongoClient mongo;
    private MongoDatabase database;
    private MongoCredential credential;

    private Integer port;
    private String host;
    private String name;
    private String user;
    private String password;

    @Autowired
    private LogMapper logMapper;
    @Autowired
    private AttributesController attributesController;
    @Autowired
    private EntitiesController entitiesController;


    public Database()
    {
        logger.info("Database created");
    }

    public void init(String host, Integer port, String name, String user, String password)
    {
        this.host = host;
        this.port = port;
        this.name = name;
        this.user = user;
        this.password = password;

        ServerAddress address = new ServerAddress(host, port);
        MongoClientOptions options = MongoClientOptions
                .builder()
                .writeConcern(WriteConcern.ACKNOWLEDGED)
                .readConcern(ReadConcern.LOCAL)
                .build();
        mongo = new MongoClient(address, options);
        database = mongo.getDatabase(name);
        credential = MongoCredential.createCredential(user, name, password.toCharArray());
    }

    public String saveDocument(String collectionName, Validator validator, Mapper mapper, Object object)
    {
        validator.validate(object, collectionName.replace(".notes", ""));

        Document document = mapper.getDocument(object);

        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.insertOne(document);
        String id = getId(document);

        saveLog(collectionName.replace(".notes", ""), "CREATE", id, null, object.toString());

        return id;
    }

    private String getId(Document document)
    {
        return ((ObjectId)document.get(MONGO_ID)).toString();
    }

    public void updateDocument(String collectionName, Validator validator, Mapper mapper, Object object)
    {
        validator.validate(object, collectionName.replace(".notes", ""));

        Document document = mapper.getDocument(object);

        Object before = getObject(collectionName, mapper, document.get(MONGO_ID).toString());

        MongoCollection<Document> collection = database.getCollection(collectionName);
        UpdateResult result = collection.updateOne(
                eq(MONGO_ID, document.get(MONGO_ID)), new Document("$set", document));
        if (result.getMatchedCount() != 1)
            throw new RuntimeException("There are no object with id " + document.get(MONGO_ID));

        saveLog(collectionName.replace(".notes", ""), "UPDATE", document.get(MONGO_ID).toString(),
                before.toString(), object.toString());
    }

    public void deleteDocument(String collectionName, Mapper mapper, String id)
    {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        Object object = getObject(collectionName, mapper, id);
        DeleteResult result = collection.deleteOne(eq(MONGO_ID, new ObjectId(id)));
        if (result.getDeletedCount() != 1)
            throw new RuntimeException(String.format("Cannot delete %s: document not found", name));

        saveLog(collectionName.replace(".notes", ""), "DELETE", id, object.toString(), null);
    }

    public void deleteDocuments(String collectionName)
    {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        long count = collection.estimatedDocumentCount();
        collection.drop();

        saveLog(collectionName.replace(".notes", ""), "DELETE", null, count, 0);
    }

    public Object[] getObjects(String collectionName, Mapper mapper, List<String> ids)
    {
        MongoCollection<Document> collection = database.getCollection(collectionName);

        FindIterable<Document> iterDoc = find(collection, ids);
        MongoCursor<Document> cursor = iterDoc.iterator();

        List<Object> objects = new ArrayList<>();
        while (cursor.hasNext())
        {
            Document document = cursor.next();
            objects.add(mapper.getObject(document));
        }
        cursor.close();

        return objects.toArray();
    }

    public Object[] getAttributes(String entityCollection)
    {
        Entity entity = getEntity(entityCollection);
        return (entity == null) ? null : getObjects(attributesController.getCollectionName(),
                attributesController.getMapper(), entity.getAttributes());
    }

    public Object getObject(String collectionName, Mapper mapper, String id)
    {
        MongoCollection<Document> collection = database.getCollection(collectionName);

        FindIterable<Document> iterDoc = collection.find(eq(MONGO_ID, new ObjectId(id))).limit(1);
        MongoCursor<Document> cursor = iterDoc.iterator();

        Object object = null;
        if (cursor.hasNext())
        {
            Document document = cursor.next();
            object = mapper.getObject(document);
        }
        cursor.close();

        return object;
    }


    public Attribute getAttribute(String name)
    {
        MongoCollection<Document> collection = database.getCollection(attributesController.getCollectionName());

        FindIterable<Document> iterDoc = collection.find(eq(NAME, name)).limit(1);
        MongoCursor<Document> cursor = iterDoc.iterator();

        Attribute attribute = null;
        if (cursor.hasNext())
        {
            Document document = cursor.next();
            attribute = (Attribute)attributesController.getMapper().getObject(document);
        }
        cursor.close();

        return attribute;
    }

    public Entity getEntity(String entityCollection)
    {
        MongoCollection<Document> collection = database.getCollection(entitiesController.getCollectionName());

        FindIterable<Document> iterDoc = collection.find(eq(COLLECTION, entityCollection)).limit(1);
        MongoCursor<Document> cursor = iterDoc.iterator();

        Entity entity = null;
        if (cursor.hasNext())
        {
            Document document = cursor.next();
            entity = (Entity)entitiesController.getMapper().getObject(document);
        }
        cursor.close();

        return entity;
    }

    public boolean isEntityExist(String entityCollection)
    {
        MongoCollection<Document> collection = database.getCollection(entitiesController.getCollectionName());

        FindIterable<Document> iterDoc = collection.find(eq(COLLECTION, entityCollection)).limit(1);

        try (MongoCursor<Document> cursor = iterDoc.iterator())
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

        FindIterable<Document> iterDoc = collection.find();
        MongoCursor<Document> cursor = iterDoc.iterator();

        List<LogRecord> result = new ArrayList<>();
        while (cursor.hasNext())
        {
            Document document = cursor.next();
            result.add((LogRecord)logMapper.getObject(document));
        }
        cursor.close();

        return result.toArray();
    }


    public Object[] getLog(int count)
    {
        MongoCollection<Document> collection = database.getCollection(LOG_COLLECTION);

        FindIterable<Document> iterDoc = collection.find().sort(new Document(MONGO_ID, -1)).limit(count);
        MongoCursor<Document> cursor = iterDoc.iterator();

        List<LogRecord> result = new ArrayList<>();
        while (cursor.hasNext())
        {
            Document document = cursor.next();
            result.add((LogRecord)logMapper.getObject(document));
        }
        cursor.close();

        return result.toArray();
    }


    private void saveLog(String collectionName, String operation, String id, Object before, Object after)
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
        metaData.put("type", file.getContentType());
        metaData.put("title", file.getOriginalFilename());
        metaData.put("size", file.getSize());

        GridFSUploadOptions options = new GridFSUploadOptions().chunkSizeBytes(8192).metadata(metaData);

        ObjectId id = bucket.uploadFromStream(file.getOriginalFilename(), file.getInputStream(), options);

        return id.toString();
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

        return file.getMetadata();
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

