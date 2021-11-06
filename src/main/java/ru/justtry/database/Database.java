package ru.justtry.database;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;
import static ru.justtry.shared.AttributeConstants.ATTRIBUTES_COLLECTION;
import static ru.justtry.shared.Constants.MONGO_ID;
import static ru.justtry.shared.EntityConstants.ENTITIES_COLLECTION;
import static ru.justtry.shared.EntityConstants.NAME;
import static ru.justtry.shared.ScaledImageConstants.ORIGINAL_ID;
import static ru.justtry.shared.ScaledImageConstants.SIZE;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.BasicDBObject;
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
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.justtry.database.sort.Sort;
import ru.justtry.database.sort.SortInfo;
import ru.justtry.mappers.LogMapper;
import ru.justtry.shared.AttributeConstants;

@RequiredArgsConstructor
@Slf4j
public class Database
{
    private static final String LOG_COLLECTION = "log";
    private static final String FILES_COLLECTION = "files";
    public static final String NOTES_FILES_COLLECTION = "notes.files";
    public static final String IMAGES_COLLECTION = "notes.images";

    private final LogMapper logMapper;
    private final Sort sort;

    private String databaseName;
    private MongoClient mongoClient;


    public void init(String host, Integer port, String name, String user, String password)
    {
        MongoCredential credential = MongoCredential.createCredential(user, name, password.toCharArray());
        ServerAddress address = new ServerAddress(host, port);
        MongoClientOptions options = MongoClientOptions
                .builder()
                .writeConcern(WriteConcern.ACKNOWLEDGED)
                .readConcern(ReadConcern.LOCAL)
                .build();
        databaseName = name;
        mongoClient = new MongoClient(address, credential, options);
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> filesCollection = database.getCollection(FILES_COLLECTION + ".files");
        filesCollection.createIndex(new BasicDBObject("md5", 1), new IndexOptions().unique(true));

        MongoCollection<Document> notesFilesCollection = database.getCollection(NOTES_FILES_COLLECTION);
        BasicDBObject notesFilesRestriction = new BasicDBObject()
                .append("noteId", 1)
                .append("attributeName", 1)
                .append("fileId", 1);
        notesFilesCollection.createIndex(notesFilesRestriction, new IndexOptions().unique(true));

        MongoCollection<Document> attributesCollection = database.getCollection(ATTRIBUTES_COLLECTION);
        attributesCollection.createIndex(new BasicDBObject(AttributeConstants.NAME, 1), new IndexOptions().unique(true));

        MongoCollection<Document> entitiesCollection = database.getCollection(ENTITIES_COLLECTION);
        entitiesCollection.createIndex(new BasicDBObject(NAME, 1), new IndexOptions().unique(true));

        log.info("Database created");
    }


    public String saveDocument(String collectionName, Document document)
    {
        MongoCollection<Document> collection = getDatabase().getCollection(collectionName);
        collection.insertOne(document);
        return ((ObjectId)document.get(MONGO_ID)).toString();
    }


    public void updateDocument(String collectionName, Document document)
    {
        MongoCollection<Document> collection = getDatabase().getCollection(collectionName);
        UpdateResult result = collection.updateOne(
                eq(MONGO_ID, document.get(MONGO_ID)), new Document("$set", document));
        if (result.getMatchedCount() != 1)
            throw new RuntimeException("There are no object with id " + document.get(MONGO_ID));
    }

    public void unsetAttribute(String collectionName, Document document, String attrName)
    {
        MongoCollection<Document> collection = getDatabase().getCollection(collectionName);
        UpdateResult result = collection.updateOne(
                eq(MONGO_ID, document.get(MONGO_ID)), new Document("$unset", new BasicDBObject(attrName, 1)));
    }


    public void deleteDocument(String collectionName, String id)
    {
        MongoCollection<Document> collection = getDatabase().getCollection(collectionName);
        DeleteResult result = collection.deleteOne(eq(MONGO_ID, new ObjectId(id)));
        if (result.getDeletedCount() != 1)
            throw new RuntimeException(String.format("Cannot delete %s: document not found", id));
    }

    public long dropCollection(String collectionName)
    {
        MongoCollection<Document> collection = getDatabase().getCollection(collectionName);
        long count = collection.estimatedDocumentCount();
        collection.drop();
        return count;
    }

    public Document getDocument(String collectionName, String id)
    {
        MongoCollection<Document> collection = getDatabase().getCollection(collectionName);
        FindIterable<Document> iterable = collection.find(eq(MONGO_ID, new ObjectId(id))).limit(1);
        try (MongoCursor<Document> cursor = iterable.iterator())
        {
            return cursor.hasNext() ? cursor.next() : null;
        }
    }

    /**
     * Get documents from requested collection ordering them by specified order in ids collection
     */
    public List<Document> getDocuments(String collectionName, List<String> values, String fieldName)
    {
        MongoCollection<Document> collection = getDatabase().getCollection(collectionName);

        FindIterable<Document> iterable = collection.find(in(fieldName, values));

        List<Document> documents = new ArrayList<>();
        try (MongoCursor<Document> cursor = iterable.iterator())
        {
            while (cursor.hasNext())
                documents.add(cursor.next());
        }

        // SortInfo documents by requested list of ids
        Map<String, Document> documentMap = new HashMap<>();
        for (Document document : documents)
            documentMap.put(document.get(fieldName).toString(), document);

        documents.clear();
        for (String id : values)
            documents.add(documentMap.get(id));

        return documents;
    }


    public List<Document> getDocuments(String collectionName, String sortField)
    {
        MongoCollection<Document> collection = getDatabase().getCollection(collectionName);
        FindIterable<Document> iterable = collection.find().sort(ascending(sortField));
        List<Document> documents = new ArrayList<>();
        try (MongoCursor<Document> cursor = iterable.iterator())
        {
            while (cursor.hasNext())
                documents.add(cursor.next());
        }

        return documents;
    }


    public List<Document> getDocuments(String collectionName, Bson filter, SortInfo sortInfo)
    {
        MongoCollection<Document> collection = getDatabase().getCollection(collectionName);
        FindIterable<Document> iterable = collection.find(filter);
        List<Document> documents = new ArrayList<>();
        try (MongoCursor<Document> cursor = iterable.iterator())
        {
            while (cursor.hasNext())
                documents.add(cursor.next());
        }

        // Inner mongodb sort wasn't good cause I had to explicitly $group result from many documents into one with
        // dynamic number of fields after $match, $unwind and $sort. I.e. there were several documents for the same
        // note, one for each attribute.
        if (sortInfo != null)
            sort.run(documents, sortInfo);

        return documents;
    }


    public Document getAttribute(String name)
    {
        MongoCollection<Document> collection = getDatabase().getCollection(ATTRIBUTES_COLLECTION);
        FindIterable<Document> iterable = collection.find(eq(NAME, name)).limit(1);
        try (MongoCursor<Document> cursor = iterable.iterator())
        {
            return cursor.hasNext() ? cursor.next() : null;
        }
    }


    public Document getEntity(String entityName)
    {
        MongoCollection<Document> collection = getDatabase().getCollection(ENTITIES_COLLECTION);
        FindIterable<Document> iterable = collection.find(eq(NAME, entityName)).limit(1);
        try (MongoCursor<Document> cursor = iterable.iterator())
        {
            return cursor.hasNext() ? cursor.next() : null;
        }
    }

    public boolean isEntityExist(String entityCollection)
    {
        MongoCollection<Document> collection = getDatabase().getCollection(ENTITIES_COLLECTION);
        FindIterable<Document> iterable = collection.find(eq(NAME, entityCollection)).limit(1);
        try (MongoCursor<Document> cursor = iterable.iterator())
        {
            return cursor.hasNext();
        }
    }


    public LogRecord[] getLog()
    {
        MongoCollection<Document> collection = getDatabase().getCollection(LOG_COLLECTION);

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
        return result.toArray(LogRecord[]::new);
    }


    public LogRecord[] getLog(int count)
    {
        MongoCollection<Document> collection = getDatabase().getCollection(LOG_COLLECTION);
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
        return result.toArray(LogRecord[]::new);
    }


    public void saveLog(String collectionName, LogRecord.Operation operation, String id, Object before, Object after)
    {
        LogRecord logRecord = new LogRecord(collectionName, operation, id, before, after);
        Document document = logMapper.getDocument(logRecord);

        MongoCollection<Document> collection = getDatabase().getCollection(LOG_COLLECTION);
        collection.insertOne(document);
    }


    public String saveFile(MultipartFile file) throws IOException
    {
        GridFSBucket bucket = GridFSBuckets.create(getDatabase(), FILES_COLLECTION);

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
        MongoCollection<Document> collection = getDatabase().getCollection(NOTES_FILES_COLLECTION);

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
                log.error(e.toString());
            }
        }
    }


    public void unlinkFilesAndNote(String noteId, String attributeName)
    {
        MongoCollection<Document> collection = getDatabase().getCollection(NOTES_FILES_COLLECTION);
        Document document = new Document()
                .append("noteId", noteId)
                .append("attributeName", attributeName);
        collection.deleteMany(document);
    }


    public void unlinkFilesAndNote(String noteId, String attributeName, Collection<String> fileIds)
    {
        if (fileIds != null && fileIds.size() > 0)
        {
            MongoCollection<Document> collection = getDatabase().getCollection(NOTES_FILES_COLLECTION);
            Document document = new Document()
                    .append("noteId", noteId)
                    .append("attributeName", attributeName);
            collection.deleteMany(and(document, in("fileId", fileIds)));
        }
    }


    public int unlinkAllFilesExcept(Set<String> fileIds)
    {
        MongoCollection<Document> notesFiles = getDatabase().getCollection(NOTES_FILES_COLLECTION);

        Set<String> fileIdsToUnlink = notesFiles.distinct("fileId", String.class).into(new HashSet<>());
        fileIdsToUnlink.removeAll(fileIds);
        for (String toUnlink : fileIdsToUnlink)
            notesFiles.deleteMany(eq("fileId", toUnlink));

        return fileIdsToUnlink.size();
    }


    public boolean isFileExists(String fileId)
    {
        MongoCollection<Document> allFiles = getDatabase().getCollection("files.files");
        FindIterable<Document> iterable = allFiles.find(eq(MONGO_ID, new ObjectId(fileId))).limit(1);
        Document file = iterable.first();

        return (file != null);
    }


    public Integer getFileSize(String fileId)
    {
        MongoCollection<Document> allFiles = getDatabase().getCollection(FILES_COLLECTION);
        FindIterable<Document> iterable = allFiles.find(eq(MONGO_ID, new ObjectId(fileId))).limit(1);
        Document file = iterable.first();

        if (file == null)
            return null;

        return (Integer)file.get("length");
    }


    public String getFileId(String md5)
    {
        GridFSBucket bucket = GridFSBuckets.create(getDatabase(), FILES_COLLECTION);

        GridFSFindIterable iterable = bucket.find(eq("md5", md5)).limit(1);
        GridFSFile file = iterable.first();

        if (file == null)
            throw new NoSuchElementException("Cannot find file with md5 " + md5);

        return file.getObjectId().toString();
    }


    public int deleteFilesOlderThan(long time)
    {
        MongoCollection notesFiles = getDatabase().getCollection(NOTES_FILES_COLLECTION);
        Set<String> usedIds = (HashSet<String>)notesFiles
                .distinct("fileId", String.class).into(new HashSet<String>());

        MongoCollection imagesCollection = getDatabase().getCollection(IMAGES_COLLECTION);

        GridFSBucket bucket = GridFSBuckets.create(getDatabase(), FILES_COLLECTION);

        GridFSFindIterable iterable = bucket.find(lt("metadata.uploaded", time));
        try (MongoCursor cursor = iterable.iterator())
        {
            int count = 0;
            while (cursor.hasNext())
            {
                GridFSFile file = (GridFSFile)cursor.next();
                if (!usedIds.contains(file.getObjectId().toString()))
                {
                    // Removing scaled images (icons)
                    String contentType = (String)file.getMetadata().get("contentType");
                    if (contentType != null && contentType.startsWith("image"))
                    {
                        Document document = new Document().append(ORIGINAL_ID, file.getObjectId().toString());
                        imagesCollection.deleteMany(document);
                    }

                    // Removing file/image itself
                    bucket.delete(file.getObjectId());
                    log.info("Removing file " + file.getMetadata().get("title"));
                    count++;
                }
            }
            return count;
        }
    }


    public InputStreamResource getFileStream(String id)
    {
        ObjectId fileId = new ObjectId(id);

        GridFSBucket bucket = GridFSBuckets.create(getDatabase(), FILES_COLLECTION);
        GridFSFindIterable iterable = bucket.find(eq(MONGO_ID, fileId)).limit(1);
        GridFSFile file = iterable.first();

        InputStreamResource resource = new InputStreamResource(bucket.openDownloadStream(file.getObjectId()));
        return resource;
    }


    public Document getMetadata(String id)
    {
        ObjectId fileId = new ObjectId(id);

        GridFSBucket bucket = GridFSBuckets.create(getDatabase(), FILES_COLLECTION);

        GridFSFindIterable iterable = bucket.find(eq(MONGO_ID, fileId)).limit(1);
        GridFSFile file = iterable.first();

        Document metadata = file.getMetadata();
        metadata.put("id", id);
        return metadata;
    }


    public List<Document> getMetadata(Collection<String> ids)
    {
        List<ObjectId> fileIds = ids.stream().map(ObjectId::new).collect(Collectors.toList());

        GridFSBucket bucket = GridFSBuckets.create(getDatabase(), FILES_COLLECTION);

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
        MongoCollection<Document> collection = getDatabase().getCollection(IMAGES_COLLECTION);
        collection.insertOne(image);
        return ((ObjectId)image.get(MONGO_ID)).toString();
    }


    public Document getImage(String identifier, int size)
    {
        MongoCollection<Document> collection = getDatabase().getCollection(IMAGES_COLLECTION);
        FindIterable<Document> iterable = collection.find(and(eq(ORIGINAL_ID, identifier), eq(SIZE, size)));
        try (MongoCursor<Document> iterator = iterable.iterator())
        {
            return (iterator.hasNext()) ? iterator.next() : null;
        }
    }


    public Document getCollectionInfo(String collectionNames)
    {
        return getDatabase().runCommand(new Document("collStats", collectionNames));
    }

    public List<Document> getFilesInfo()
    {
        List<Bson> aggregation = new ArrayList<>();
        Bson unwind = Aggregates.unwind("$metadata");
        Bson group = Aggregates.group("$metadata.contentType",
                Accumulators.addToSet("contentType", "$metadata.contentType"),
                Accumulators.sum("size", "$length"),
                Accumulators.sum("count", 1L));
        Bson project = Aggregates.project(
                Projections.fields(excludeId(), include("contentType", "size", "count")));
        Bson sort = Aggregates.sort(ascending("contentType"));

        aggregation.add(unwind);
        aggregation.add(group);
        aggregation.add(project);
        aggregation.add(sort);

        MongoCollection<Document> files = getDatabase().getCollection("files.files");
        List<Document> result = files.aggregate(aggregation).into(new ArrayList<>());
        return result;
    }

    public Document getDatabaseInfo()
    {
        return getDatabase().runCommand(new Document("dbstats", 1));
    }


    /**
     * Creates thread-safe MongoDatabase instance.
     * As it said, every instance of MongoDatabase, created via MongoClient, is thread-safe.
     * http://mongodb.github.io/mongo-java-driver/3.5/driver/getting-started/quick-start/
     */
    private MongoDatabase getDatabase()
    {
        return mongoClient.getDatabase(databaseName);
    }
}


