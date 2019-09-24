package ru.justtry.notes;

import org.bson.Document;
import org.springframework.stereotype.Component;
import ru.justtry.shared.Validator;

@Component
public class NoteValidator implements Validator
{
    @Override
    public void validate(Object object)
    {

    }

    @Override
    public void validate(Document document)
    {

    }
}
