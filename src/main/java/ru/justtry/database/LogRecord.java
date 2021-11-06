package ru.justtry.database;

import java.util.Date;

import lombok.Data;

@Data
public class LogRecord
{
    public enum Operation
    {
        CREATE,
        UPDATE,
        DELETE
    }

    private long time;
    private String collection;
    private Operation operation;
    private String id;
    private Object before;
    private Object after;


    public LogRecord()
    {

    }

    public LogRecord(String collection, Operation operation, String id, Object before, Object after)
    {
        this.collection = collection;
        this.operation = operation;
        this.id = id;
        this.before = before;
        this.after = after;
        this.time = new Date().getTime();
    }
}

