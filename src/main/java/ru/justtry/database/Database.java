package ru.justtry.database;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import ru.justtry.attributes.*;
import ru.justtry.shared.Mapper;
import ru.justtry.shared.Validator;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static ru.justtry.attributes.AttributeConstants.*;
import static ru.justtry.attributes.EntityConstants.*;
import static ru.justtry.shared.Constants.*;


//@Component
//@PropertySource("classpath:application.properties")
//@Component
//@Scope("singleton")
//@Named("database")
public class Database
{
    @Inject
    AttributeValidator attributeValidator;
    @Inject
    AttributeMapper attributeMapper;
    @Inject
    EntityValidator entityValidator;
    @Inject
    EntityMapper entityMapper;

    final static Logger logger = Logger.getLogger(Database.class);

    private MongoClient mongo;
    private MongoDatabase database;
    private MongoCredential credential;

    private Integer port;
    private String host;
    private String name;
    private String user;
    private String password;

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

        mongo = new MongoClient(host, port);
        database = mongo.getDatabase(name);
        credential = MongoCredential.createCredential(user, name, password.toCharArray());
    }

    public String saveAttribute(Attribute attribute)
    {
        attributeValidator.validate(attribute);

        MongoCollection<Document> collection = database.getCollection(ATTRIBUTES_COLLECTION);
        Document document = attributeMapper.getDocument(attribute);
        collection.insertOne(document);

        return getId(document);
    }


    public Attribute getAttribute(String name)
    {
        return (Attribute)getObject(ATTRIBUTES_COLLECTION, attributeMapper, name);

//        MongoCollection<Document> collection = database.getCollection(ATTRIBUTES_COLLECTION);
//
//        BasicDBObject whereQuery = new BasicDBObject();
//        whereQuery.put(NAME, name);
//        FindIterable<Document> iterDoc = collection.find(whereQuery).limit(1);
//        MongoCursor<Document> cursor = iterDoc.iterator();
//
//        Attribute attribute = null;
//        if (cursor.hasNext())
//        {
//            Document document = cursor.next();
//            attribute = attributeMapper.getObject(document);
//        }
//        cursor.close();
//
//        return attribute;
    }

//    public Object[] getAttributes()
//    {
//        return getObjects(ATTRIBUTES_COLLECTION, attributeMapper);
//
////        MongoCollection<Document> collection = database.getCollection(ATTRIBUTES_COLLECTION);
////
////        FindIterable<Document> iterDoc = collection.find();
////        MongoCursor<Document> cursor = iterDoc.iterator();
////
////        List<Attribute> attributes = new ArrayList<>();
////        while (cursor.hasNext())
////        {
////            Document document = cursor.next();
////            attributes.add((Attribute)attributeMapper.getObject(document));
////        }
////        cursor.close();
////
////        return attributes.toArray();
//    }

    public String saveDocument(String collectionName, Validator validator, Mapper mapper, Map<String, Object> values)
    {
        Document document = mapper.getDocument(null, values);
        validator.validate(document);

        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.insertOne(document);

        return getId(document);
    }

    public String saveDocument(String collectionName, Validator validator, Mapper mapper, Object object)
    {
        validator.validate(object);

        Document document = mapper.getDocument(object);

        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.insertOne(document);

        return getId(document);
    }


    public String saveEntity(Entity entity)
    {
        entityValidator.validate(entity);

        //BasicDBObject dbObject = new BasicDBObject(ATTRIBUTES, "value").append("array", entity.getAttributes());

//        for (String attribute : entity.getAttributes())
//        {
//            List<BasicDBObject> attributes = new ArrayList<>();
//            attributes.add(new BasicDBObject("key", attribute));
//        }
        MongoCollection<Document> collection = database.getCollection(ENTITIES_COLLECTION);
        Document document = new Document()
                .append(NAME, entity.getName())
                .append(ATTRIBUTES, entity.getAttributes());
        collection.insertOne(document);

        return getId(document);
    }

    private String getId(Document document)
    {
        return ((ObjectId)document.get(MONGO_ID)).toString();
    }

//    public void updateEntity(Entity entity)
//    {
//        entityValidator.validate(entity);
//
//        MongoCollection<Document> collection = database.getCollection(ENTITIES_COLLECTION);
//        Document document = new Document()
//                .append(NAME, entity.getName())
//                .append(ATTRIBUTES, entity.getAttributes());
//
//        UpdateResult result = collection.updateOne(
//                eq(MONGO_ID, new ObjectId(entity.getId())), new Document("$set", document));
//        if (result.getModifiedCount() != 1)
//            throw new RuntimeException("There are no such entity to update");
//    }

    public void updateDocument(String collectionName, Validator validator, Mapper mapper,
                               String id, Map<String, Object> values)
    {
        Document document = mapper.getDocument(id, values);
        validator.validate(document);

        MongoCollection<Document> collection = database.getCollection(collectionName);
        UpdateResult result = collection.updateOne(
                eq(MONGO_ID, document.get(MONGO_ID)), new Document("$set", document));
        if (result.getModifiedCount() != 1)
            throw new RuntimeException("There are no such entity to update");
    }

    public void updateDocument(String collectionName, Validator validator, Mapper mapper, Object object)
    {
        validator.validate(object);

        Document document = mapper.getDocument(object);

        MongoCollection<Document> collection = database.getCollection(collectionName);
        UpdateResult result = collection.updateOne(
                eq(MONGO_ID, document.get(MONGO_ID)), new Document("$set", document));
        if (result.getModifiedCount() != 1)
            throw new RuntimeException("There are no such object to update");
    }

//    public void deleteEntity(String name)
//    {
//        MongoCollection<Document> collection = database.getCollection(ENTITIES_COLLECTION);
//
//        BasicDBObject whereQuery = new BasicDBObject();
//        whereQuery.put(NAME, name);
//        DeleteResult result = collection.deleteOne(whereQuery);
//        if (result.getDeletedCount() != 1)
//            throw new RuntimeException(String.format("Cannot delete %s: document not found", name));
//    }

    public void deleteDocument(String collectionName, String id)
    {
        MongoCollection<Document> collection = database.getCollection(collectionName);

        DeleteResult result = collection.deleteOne(eq(MONGO_ID, new ObjectId(id)));
        if (result.getDeletedCount() != 1)
            throw new RuntimeException(String.format("Cannot delete %s: document not found", name));
    }

    public void deleteDocuments(String collectionName)
    {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.drop();
    }

//    public Entity getEntity(String id)
//    {
//        return (Entity)getObject(ENTITIES_COLLECTION, entityMapper, id);
//
////        MongoCollection<Document> collection = database.getCollection(ENTITIES_COLLECTION);
////
////        BasicDBObject whereQuery = new BasicDBObject();
////        whereQuery.put(NAME, id);
////        FindIterable<Document> iterDoc = collection.find(whereQuery).limit(1);
////        MongoCursor<Document> cursor = iterDoc.iterator();
////
////        Entity entity = null;
////        if (cursor.hasNext())
////        {
////            Document document = cursor.next();
////            entity = entityMapper.getObject(document);
////        }
////        cursor.close();
////
////        return entity;
//    }

    public Object[] getEntities()
    {
        return getObjects(ENTITIES_COLLECTION, entityMapper);
    }

    public Object[] getObjects(String collectionName, Mapper mapper)
    {
        MongoCollection<Document> collection = database.getCollection(collectionName);

        FindIterable<Document> iterDoc = collection.find();
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

    public Object getObject(String collectionName, Mapper mapper, String id)
    {
        MongoCollection<Document> collection = database.getCollection(collectionName);

//        BasicDBObject whereQuery = new BasicDBObject();
//        whereQuery.put(MONGO_ID, id);
        //FindIterable<Document> iterDoc = collection.find(whereQuery).limit(1);
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

    public void saveDatabaseUnit(String collection)
    {

    }

    public void createTestData()
    {
        MongoCollection<Document> collection = database.getCollection("sampleCollection");

        Document document = new Document("title", "MongoDB")
                .append("id", 1)
                .append("description", "database")
                .append("likes", 100)
                .append("url", "http://www.tutorialspoint.com/mongodb/")
                .append("by", "tutorials point");
        collection.insertOne(document);

        FindIterable<Document> iterDoc = collection.find();
        int i = 1;

        // Getting the iterator
        Iterator it = iterDoc.iterator();

        while (it.hasNext()) {
            System.out.println(it.next());
            i++;
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

