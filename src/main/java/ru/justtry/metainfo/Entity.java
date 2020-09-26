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
    private String name;
    private String title;
    private boolean visible;
    private String keyAttribute;
    private String sortAttribute;
    private String sortDirection;
    private List<String> attributes = new ArrayList<>();
    private List<String> comparedAttributes = new ArrayList<>();
    private List<String> visibleAttributes = new ArrayList<>();

    @Override
    public String toString()
    {
        return String.format("%s (title=%s, visible=%s, keyAttribute=%s, sortAttribute=%s, sortDirection=%s, "
                        + "attributes=[%s], comparedAttributes=[%s], visibleAttributes=[%s])",
                name, title, String.valueOf(visible), keyAttribute, sortAttribute, sortDirection,
                String.join(", ", attributes), String.join(", ", comparedAttributes),
                String.join(", ", visibleAttributes));
    }


    public boolean hasAttribute(String attributeId)
    {
        return attributes.stream().anyMatch(x -> x.contentEquals(attributeId));
    }
}