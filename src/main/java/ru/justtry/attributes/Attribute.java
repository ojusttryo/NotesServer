package ru.justtry.attributes;

import ru.justtry.shared.Identifiable;

public class Attribute extends Identifiable
{
    private String name = "";
    private String method = "none";
    private boolean visible = true;
    /**
     * Value type. Possible types:
     * - text - single line string
     * - textarea - multiline string
     * - int - numeric field
     */
    private String type;
    private String minWidth = "0";
    private String maxWidth = "0";

    private String minValue;
    private String maxValue;
    private String defaultValue;
    private int linesCount = 1;
    private String alignment = "left";


    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    public boolean getVisible()
    {
        return visible;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getMinWidth()
    {
        return minWidth;
    }

    public void setMinWidth(String minWidth)
    {
        this.minWidth = minWidth;
    }

    public String getMaxWidth()
    {
        return maxWidth;
    }

    public void setMaxWidth(String maxWidth)
    {
        this.maxWidth = maxWidth;
    }

    public int getLinesCount()
    {
        return linesCount;
    }

    public void setLinesCount(int linesCount)
    {
        this.linesCount = linesCount;
    }

    public String getAlignment()
    {
        return alignment;
    }

    public void setAlignment(String alignment)
    {
        this.alignment = alignment;
    }


    public String getMinValue()
    {
        return minValue;
    }

    public void setMinValue(String minValue)
    {
        this.minValue = minValue;
    }

    public String getMaxValue()
    {
        return maxValue;
    }

    public void setMaxValue(String maxValue)
    {
        this.maxValue = maxValue;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

}
