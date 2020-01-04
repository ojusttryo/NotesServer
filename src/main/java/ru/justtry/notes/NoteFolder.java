package ru.justtry.notes;

import ru.justtry.shared.Identifiable;

public class NoteFolder extends Identifiable
{
    private String folderId;
    private String name;
    private Integer level;

    public Integer getLevel()
    {
        return level;
    }

    public void setLevel(Integer level)
    {
        this.level = level;
    }

    public String getFolderId()
    {
        return folderId;
    }

    public void setFolderId(String folderId)
    {
        this.folderId = folderId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
