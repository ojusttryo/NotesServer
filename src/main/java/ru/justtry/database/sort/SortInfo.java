package ru.justtry.database.sort;

import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;

import org.bson.conversions.Bson;

import lombok.Data;
import ru.justtry.metainfo.Attribute;

@Data
public class SortInfo
{

    private Attribute attribute;
    private Direction direction;


    public SortInfo(Attribute attribute, String direction)
    {
        this.attribute = attribute;
        this.direction = Direction.get(direction);
    }

    public Bson getDirectionAsBson()
    {
        return (direction == Direction.DESCENDING) ? descending(attribute.getName()) : ascending(attribute.getName());
    }

    public int getDirectionAsInt()
    {
        return (direction == Direction.DESCENDING) ? -1 : 1;
    }

}
