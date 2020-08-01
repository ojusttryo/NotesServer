package ru.justtry.metainfo;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import ru.justtry.shared.AttributeConstants.DefaultValue;
import ru.justtry.shared.AttributeConstants.Method;
import ru.justtry.shared.Identifiable;

/**
 * The meta information about some field of the note. This class describes how to use fields of notes.
 *
 * For example, there are information about how to display some value on the notes table in user interface.
 * Also there are some information to check new value before send to server or save in database.
 */
@Data
public class Attribute extends Identifiable
{
    /**
     * The unique name of attribute.
     */
    private String name = "";

    /**
     * The title of attribute, which will be displayed on the form.
     */
    private String title = "";

    /**
     * Method responsible for aggregation operations on folders.
     * Possible methods:
     * - none - do nothing, just empty field
     * - folder name - the name of the folder
     * - avg - get the average attribute value from the notes inside folder
     * - count - count of objects in folder
     */
    private String method = Method.NONE;

    /**
     * The visibility of column with this attribute in table
     */
    private Boolean visible = DefaultValue.VISIBLE;

    /**
     * Value type.
     * Possible types:
     * - text - single line string
     * - textarea - multiline string
     * - number - numeric field
     * - select - drop-down list with single selected value, represents enum value
     * - multiselect - drop-down list that allows multiple selections, represents list of enum selectOptions
     * - checkbox - boolean value
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
     * Step of numeric value. Keep in string for precision.
     */
    private String step = DefaultValue.STEP.toString();

    /**
     * Lines count to display this attribute.
     * Except textarea the value is ignored.
     */
    private Integer linesCount = DefaultValue.LINES_COUNT;

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
    @Setter(AccessLevel.NONE)
    private List<String> selectOptions;

    public void setSelectOptions(List<String> selectOptions)
    {
        if (selectOptions == null)
            return;

        this.selectOptions = new ArrayList<>();
        for (String option : selectOptions)
        {
            option = option.trim();
            if (option.length() > 0)
                this.selectOptions.add(option);
        }
    }

    @Override
    public String toString()
    {
        String options = selectOptions == null ? "null" :
                Stream.of(selectOptions).map(Object::toString).collect(Collectors.joining(", "));

        return String.format("%s (title=%s; method=%s; visible=%s; required=%s; type=%s; minWidth=%s; maxWidth=%s, "
                        + "min=%s; max=%s; defaultValue=%s; linesCount=%s; alignment=%s; regex=%s; selectOptions=%s",
                name, title, method, visible, required, type,
                ofNullable(minWidth).orElse("null"), ofNullable(maxWidth).orElse("null"),
                ofNullable(min).orElse("null"), ofNullable(max).orElse("null"),
                ofNullable(defaultValue).orElse("null"), linesCount == null ? "null" : linesCount.toString(),
                ofNullable(alignment).orElse("null"), ofNullable(regex).orElse("null"), options);
    }
}
