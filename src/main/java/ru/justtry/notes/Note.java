package ru.justtry.notes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Data;
import ru.justtry.shared.Identifiable;

@Data
public class Note extends Identifiable
{
    private String folderId;
    // Such weird data type is because I need to parse array from JSON to key-value type. Something like that:
    // "metainfo": [
    //		{ "5d8a5a87602f051474ea6a4e": "Avengers 5" },
    //		{ "5d8a5b17602f051474ea6a50": "Finished" },
    //		{ "5d8a6035afa6f80313f050c3": 2025 }
    //	]
    // And it is a List type because attributes should be ordered
    private List<Map.Entry<String, Object>> attributes = new ArrayList<>();

    @Override
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        for (Map.Entry<String, Object> attribute : attributes)
        {
            Object value = (attribute.getValue() == null) ? "" : attribute.getValue();
            buffer.append(value + ";");
        }
        buffer.deleteCharAt(buffer.length() - 1);
        return buffer.toString();
    }
}
