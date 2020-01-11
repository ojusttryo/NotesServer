package ru.justtry.metainfo;

import java.util.Arrays;
import java.util.List;

import ru.justtry.shared.AttributeConstants.DefaultValue;
import ru.justtry.shared.Identifiable;

/**
 * The meta information about some field of the note. This class describes how to use fields of notes.
 *
 * For example, there are information about how to display some value on the notes table in user interface.
 * Also there are some information to check new value before send to server or save in database.
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
     * - folder name - the name of the folder
     * - avg - get the average attribute value from the notes inside folder
     * - count - count of objects in folder
     */
    private String method = null;

    /**
     * The visibility of column with this attribute in table
     */
    private boolean visible = DefaultValue.VISIBLE;

    /**
     * Value type.
     * Possible types:
     * - text - single line string
     * - textarea - multiline string
     * - int - numeric field
     * - float - number with floating point
     * - select - drop-down list with single selected value, represents enum value
     * - multiselect - drop-down list that allows multiple selections, represents list of enum selectValues
     * - checkbox - boolean value
     * - year - numeric field limited by value and size
     * - inc - incremented number, which has plus sign beside
     * - url - the URL
     * - etc
     */
    private String type = DefaultValue.TYPE;

    /**
     * Minimum width of the field with this attribute.
     * Could keep value like "200" and "20em"
     */
    private String minWidth = null;

    /**
     * Maximum width of the field with this attribute.
     * Could keep value like "200" and "20em".
     */
    private String maxWidth = null;

    /**
     * For number types applies as the lower bound. For text types - as the minimum length.
     * Could contains different data types, so the field is String
     */
    private String min = null;

    /**
     * For int types applies as the upper bound. For text types - as the maximum length.
     * Could contains different data types, so the field is String
     */
    private String max = null;

    /**
     * Default value of attribute.
     */
    private String defaultValue = null;

    /**
     * Lines count to display this attribute.
     * Except textarea the value is ignored.
     */
    private Integer linesCount = null;

    /**
     * The alignment of the attribute in data table.
     */
    private String alignment = DefaultValue.ALIGNMENT;

    /**
     * Specifies if the attribute must be filled.
     */
    private Boolean required = DefaultValue.REQUIRED;

    /**
     * The regular expression is used to check the correctness of value.
     */
    private String regex;

    /**
     * The values of drop-down list (enum)
     */
    private List<String> selectValues;

    /**
     * Get the regular expression.
     * @return {@link #regex regular expression}
     */
    public String getRegex()
    {
        return regex;
    }

    public List<String> getSelectValues()
    {
        return selectValues;
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

    public void setSelectValues(String... selectValues)
    {
        this.selectValues = Arrays.asList(selectValues);
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


    public String getMin()
    {
        return min;
    }

    public void setMin(String min)
    {
        this.min = min;
    }

    public String getMax()
    {
        return max;
    }

    public void setMax(String max)
    {
        this.max = max;
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
