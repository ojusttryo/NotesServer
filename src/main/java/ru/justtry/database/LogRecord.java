package ru.justtry.database;

import java.util.Date;

public class LogRecord
{
    private long time;
    private String collection;
    private String operation;
    private String id;
    private Object before;
    private Object after;


    public LogRecord()
    {

    }

    public LogRecord(String collection, String operation, String id, Object before, Object after)
    {
        this.collection = collection;
        this.operation = operation;
        this.id = id;
        this.before = before;
        this.after = after;
        this.time = new Date().getTime();
    }

    public String getCollection()
    {
        return collection;
    }

    public void setCollection(String collection)
    {
        this.collection = collection;
    }

    public String getOperation()
    {
        return operation;
    }

    public void setOperation(String operation)
    {
        this.operation = operation;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Object getBefore()
    {
        return before;
    }

    public void setBefore(Object before)
    {
        this.before = before;
    }

    public Object getAfter()
    {
        return after;
    }

    public void setAfter(Object after)
    {
        this.after = after;
    }

    public long getTime()
    {
        return time;
    }

    public void setTime(long time)
    {
        this.time = time;
    }
}

