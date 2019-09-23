package ru.justtry.shared;

import org.bson.Document;

public interface Validator
{
    void validate(Object object);

    void validate(Document document);
}
