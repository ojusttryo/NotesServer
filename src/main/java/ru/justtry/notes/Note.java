package ru.justtry.notes;

import ru.justtry.shared.Identifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Note extends Identifiable
{
    private String folderId;
    // Such weird data type is because I need to parse array from JSON to key-value type. Something like that:
    // "attributes": [
    //		{ "5d8a5a87602f051474ea6a4e": "Avengers 5" },
    //		{ "5d8a5b17602f051474ea6a50": "Finished" },
    //		{ "5d8a6035afa6f80313f050c3": 2025 }
    //	]
    private List<Map.Entry<String, Object>> attributes = new ArrayList<>();

    public List<Map.Entry<String, Object>> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(List<Map.Entry<String, Object>> attributes)
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
