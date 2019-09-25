package ru.justtry.attributes;

import ru.justtry.shared.Identifiable;

/**
 * The meta information about some attribute of the note.
 */
public class Attribute extends Identifiable
{
    /**
     * Name is used as the title of the columns and forms.
     */
    private String name = "";

    /**
     * Method responsible for aggregation operations on folders.
     * Possible methods:
     * - none - do nothing, just empty field
     * - name - the name of the folder
     * - avg - get the average attribute value from the notes inside folder
     * - count - count of objects in folder
     */
    private String method = "none";

    /**
     * The visibility of column with this attribute in table
     */
    private boolean visible = true;

    /**
     * Value type.
     * Possible types:
     * - text - single line string
     * - textarea - multiline string
     * - int - numeric field
     * - float - number with floating point
     */
    private String type;

    /**
     * Minimum width of the field with this attribute
     */
    private String minWidth = "0";

    /**
     * Maximum width of the field with this attribute
     */
    private String maxWidth = "0";

    /**
     * Minimum value of the attribute.
     * For int types applies as the lower bound. For text types - as the minimum length.
     */
    private String minValue;

    /**
     * Maximum value of the attribute.
     * For int types applies as the upper bound. For text types - as the maximum length.
     */
    private String maxValue;

    /**
     * Default value of attribute.
     */
    private String defaultValue;

    /**
     * Lines count to display this attribute.
     * Except textarea the value is ignored.
     */
    private Integer linesCount = 1;

    /**
     * The alignment of the attribute in data table.
     */
    private String alignment = "left";

    /**
     * Specifies if the attribute must be filled.
     */
    private Boolean required = false;

    /**
     * The regular expression is used to check the correctness of value.
     */
    private String regex;

    /**
     * Get the regular expression.
     * @return {@link #regex regular expression}
     */
    public String getRegex()
    {
        return regex;
    }

    /**
     * Set the {@link #regex regular expression}
     */
    public void setRegex(String regex)
    {
        this.regex = regex;
    }

    public Boolean getRequired()
    {
        return required;
    }

    public void setRequired(Boolean required)
    {
        this.required = required;
    }

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

    public Integer getLinesCount()
    {
        return linesCount;
    }

    public void setLinesCount(Integer linesCount)
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
