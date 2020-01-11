package ru.justtry.metainfo;

import java.util.ArrayList;
import java.util.List;

import ru.justtry.shared.Identifiable;

/**
 * The metadata of some notes entity (Movies, Books, etc).
 */
public class Entity extends Identifiable
{
    private String name;
    private List<String> attributes = new ArrayList<>();

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<String> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(List<String> attributes)
    {
        this.attributes = attributes;
    }
}