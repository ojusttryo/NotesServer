package ru.justtry.mappers;

import org.bson.Document;

import java.util.Map;

public abstract class Mapper
{
    public abstract Object getObject(Document document);
    public abstract Document getDocument(Object object);


    protected String getStringOrNull(Document document, String key)
    {
        return (document.containsKey(key) && document.get(key) != null) ? document.get(key).toString() : null;
    }
}
