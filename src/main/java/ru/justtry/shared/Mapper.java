package ru.justtry.shared;

import org.bson.Document;
import ru.justtry.attributes.Attribute;

import java.util.Map;

public interface Mapper
{
    Object getObject(Document document);

    // Не делать общий метод, т.к. ожидаются более сложные типы данных в values.
    Document getDocument(String id, Map<String, Object> values);
}
