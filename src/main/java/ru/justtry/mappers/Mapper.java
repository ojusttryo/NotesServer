package ru.justtry.mappers;

import java.util.List;

import org.bson.Document;

import ru.justtry.shared.Identifiable;

public abstract class Mapper
{
    public abstract Identifiable getObject(Document document);
    public abstract Identifiable[] getObjects(List<Document> documents);
    public abstract Document getDocument(Identifiable object);


    protected String getStringOrNull(Document document, String key)
    {
        return (document.containsKey(key) && document.get(key) != null) ? document.get(key).toString() : null;
    }
}
