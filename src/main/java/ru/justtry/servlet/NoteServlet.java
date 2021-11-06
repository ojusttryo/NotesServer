package ru.justtry.servlet;

import java.util.HashMap;
import java.util.Map;

import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.dictionary.Type;

/**
 *
 */
public class NoteServlet
{
    private final ThreadLocal<Map<String, Integer>> rowNumbers = new InheritableThreadLocal<>();



    private void fillRowNumberAttributes(Attribute[] attributes)
    {
        // Any attribute might represent a row number. And there should be any place to track them across cycle.
        rowNumbers.set(new HashMap<>());
        for (Attribute attribute : attributes)
        {
            if (attribute.getTypeAsEnum() == Type.ROW_NUMBER)
                rowNumbers.get().put(attribute.getName(), 1);
        }
    }

}
