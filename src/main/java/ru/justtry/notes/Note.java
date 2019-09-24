package ru.justtry.notes;

import java.util.List;

public class Note
{
    private String entity;
    private List<NoteAttribute> attributes;


    public String getEntity()
    {
        return entity;
    }

    public void setEntity(String entity)
    {
        this.entity = entity;
    }

    public List<NoteAttribute> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(List<NoteAttribute> attributes)
    {
        this.attributes = attributes;
    }
}
