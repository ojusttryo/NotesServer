package ru.justtry.notes;

import ru.justtry.shared.Identifiable;

import java.util.List;

public class Note extends Identifiable
{
    private String folderId;
    private List<NoteAttribute> attributes;

    public List<NoteAttribute> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(List<NoteAttribute> attributes)
    {
        this.attributes = attributes;
    }

    public String getFolderId()
    {
        return folderId;
    }

    public void setFolderId(String folderId)
    {
        this.folderId = folderId;
    }
}
