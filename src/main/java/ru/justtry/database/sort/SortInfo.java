package ru.justtry.database.sort;

import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;

import org.bson.conversions.Bson;

import lombok.Data;
import ru.justtry.metainfo.Attribute;

@Data
public class SortInfo
{
    public enum Direction
    {
        ASCENDING("ascending"),
        DESCENDING("descending");

        public final String title;

        public static Direction get(String direction)
        {
            switch (direction)
            {
            case "ascending": return ASCENDING;
            case "descending": return DESCENDING;
            default: return null;
            }
        }

        Direction(String titile)
        {
            this.title = titile;
        }
    }

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
