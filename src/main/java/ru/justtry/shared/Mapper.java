package ru.justtry.shared;

import org.bson.Document;
import ru.justtry.attributes.Attribute;

import java.util.Map;

public abstract class Mapper
{
    public abstract Object getObject(Document document);

    // Не делать общий метод, т.к. ожидаются более сложные типы данных в values.
    public abstract Document getDocument(String id, Map<String, Object> values);

    public abstract Document getDocument(Object object);

    protected String getStringOrNull(Document document, String key)
    {
        return (document.containsKey(key) && document.get(key) != null) ? document.get(key).toString() : null;
    }
}
