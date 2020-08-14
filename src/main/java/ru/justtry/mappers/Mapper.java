package ru.justtry.mappers;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import ru.justtry.shared.Identifiable;

public abstract class Mapper
{
    public abstract Identifiable getObject(Document document);
    public abstract Document getDocument(Identifiable object);


    public Identifiable[] getObjects(List<Document> documents)
    {
        List<Identifiable> objects = new ArrayList<>();
        for (Document document : documents)
            objects.add(getObject(document));
        return objects.toArray(new Identifiable[0]);
    }


    protected String getStringOrNull(Document document, String key)
    {
        return (document.containsKey(key) && document.get(key) != null) ? document.get(key).toString() : null;
    }
}
