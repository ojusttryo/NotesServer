package ru.justtry.metainfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ru.justtry.shared.Identifiable;

/**
 * The metadata of some notes entity (Movies, Books, etc).
 */
public class Entity extends Identifiable
{
    private String collection;
    private String title;
    private List<String> attributes = new ArrayList<>();    // identifiers

    public String getCollection()
    {
        return collection;
    }

    public void setCollection(String collection)
    {
        this.collection = collection;
    }

    public List<String> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(List<String> attributes)
    {
        this.attributes = attributes;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    @Override
    public String toString()
    {
        return String.format("%s (title=%s, attributes=[%s])", collection, title, String.join(", ", attributes));
    }
}