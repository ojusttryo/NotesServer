package ru.justtry.mappers;

import static ru.justtry.shared.Constants.ID;
import static ru.justtry.shared.LogConstants.COLLECTION;
import static ru.justtry.shared.LogConstants.AFTER;
import static ru.justtry.shared.LogConstants.BEFORE;
import static ru.justtry.shared.LogConstants.OPERATION;
import static ru.justtry.shared.LogConstants.TIME;


import org.bson.Document;
import org.springframework.stereotype.Component;

import ru.justtry.database.LogRecord;

@Component
public class LogMapper
{
    public Object getObject(Document document)
    {
        LogRecord logRecord = new LogRecord();
        logRecord.setCollection(document.get(COLLECTION).toString());
        logRecord.setOperation(document.get(OPERATION).toString());
        logRecord.setId(getStringOrNull(document, ID));
        logRecord.setBefore(document.get(BEFORE));
        logRecord.setAfter(document.get(AFTER));
        logRecord.setTime((long)document.get(TIME));
        return logRecord;
    }

    public Document getDocument(Object object)
    {
        LogRecord logRecord = (LogRecord)object;

        Document document = new Document()
                .append(COLLECTION, logRecord.getCollection())
                .append(OPERATION, logRecord.getOperation())
                .append(TIME, logRecord.getTime());

        if (logRecord.getId() != null)
            document.append(ID, logRecord.getId());

        if (logRecord.getBefore() != null)
            document.append(BEFORE, logRecord.getBefore());

        if (logRecord.getAfter() != null)
            document.append(AFTER, logRecord.getAfter());

        return document;
    }


    protected String getStringOrNull(Document document, String key)
    {
        return (document.containsKey(key) && document.get(key) != null) ? document.get(key).toString() : null;
    }
}
