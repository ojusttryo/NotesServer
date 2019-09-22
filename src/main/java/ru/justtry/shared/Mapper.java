package ru.justtry.shared;

import org.bson.Document;
import ru.justtry.attributes.Attribute;

public interface Mapper
{
    Object get(Document document);
}
