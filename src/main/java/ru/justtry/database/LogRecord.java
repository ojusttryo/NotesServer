package ru.justtry.database;

import java.util.Date;

import lombok.Data;

@Data
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
}

