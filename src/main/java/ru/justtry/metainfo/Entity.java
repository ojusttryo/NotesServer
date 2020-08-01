package ru.justtry.metainfo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import ru.justtry.shared.Identifiable;

/**
 * The metadata of some kind of note entity (Movies, Books, etc).
 */
@Data
public class Entity extends Identifiable
{
    private String collection;
    private String title;
    private boolean visible;
    private List<String> attributes = new ArrayList<>();    // identifiers

    @Override
    public String toString()
    {
        return String.format("%s (title=%s, visible=%s attributes=[%s])", collection, title,
                String.valueOf(visible), String.join(", ", attributes));
    }
}