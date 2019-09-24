package ru.justtry.notes;

import ru.justtry.shared.Identifiable;

import java.util.List;

public class Note extends Identifiable
{
    private List<NoteAttribute> attributes;

    public List<NoteAttribute> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(List<NoteAttribute> attributes)
    {
        this.attributes = attributes;
    }
}
