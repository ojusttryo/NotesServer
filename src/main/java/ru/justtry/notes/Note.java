package ru.justtry.notes;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import ru.justtry.shared.Identifiable;

@Data
public class Note extends Identifiable
{
    private String folderId;
    private boolean hidden = false;
    private Map<String, Object> attributes = new HashMap<>();

    @Override
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("hidden=" + hidden + " ");
        for (String name : attributes.keySet())
        {
            Object value = (attributes.get(name) == null) ? "" : attributes.get(name);
            buffer.append(value + ";");
        }
        if (buffer.length() > 0)
            buffer.deleteCharAt(buffer.length() - 1);
        return buffer.toString();
    }
}
