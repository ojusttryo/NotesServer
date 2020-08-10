package ru.justtry.metainfo;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import ru.justtry.shared.AttributeConstants.DefaultValue;
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
    public enum Method
    {
        NONE("none"),                   // does nothing, just empty field
        FOLDER_NAME("folder name"),     // the name of the folder containing notes
        AVG("avg"),                     // the average attributes value from the notes inside folder
        COUNT("count");                 // count of objects in folder

        public final String title;

        public static Method get(String method)
        {
            switch (method)
            {
            case "none": return NONE;
            case "folder name": return FOLDER_NAME;
            case "avg": return AVG;
            case "count": return COUNT;
            default: return null;
            }
        }

        Method(String title)
        {
            this.title = title;
        }
    }

    public enum Type
    {
        TEXT("text"),                   // single line string
        TEXT_AREA("textarea"),          // multiline string
        NUMBER("number"),               // numeric field (double or integer)
        SELECT("select"),               // drop-down list with single selected value, represents enum value
        MULTI_SELECT("multiselect"),    // drop-down list that allows multiple selections
        CHECKBOX("checkbox"),           // boolean value
        INC("inc"),                     // incremented number, which has plus sign beside
        URL("url"),                     // the URL
        SAVE_TIME("save time"),         // timestamp when the note is saved
        UPDATE_TIME("update time"),     // timestamp when the note is updated
        USER_DATE("user date"),         // custom date (yyyy-mm-dd)
        USER_TIME("user time"),         // custom time (hh:mm, 24-hours format)
        FILE("file"),
        IMAGE("image");

        public final String title;

        public static boolean isTextType(Type type)
        {
            return (type == TEXT || type == TEXT_AREA);
        }

        public static boolean isTextType(String type)
        {
            Type t = get(type);
            return (t != null) && isTextType(t);
        }

        public static boolean isNumericType(Type type)
        {
            return (type == NUMBER || type == INC);
        }

        public static boolean isNumericType(String type)
        {
            Type t = get(type);
            return (t != null) && isNumericType(t);
        }

        public static boolean isSelectType(Type type)
        {
            return (type == SELECT || type == MULTI_SELECT);
        }

        public static boolean isTimestampType(Type type)
        {
            return (type == SAVE_TIME || type == UPDATE_TIME);
        }

        public static Type get(String type)
        {
            switch (type)
            {
            case "text": return TEXT;
            case "textarea": return TEXT_AREA;
            case "number": return NUMBER;
            case "select": return SELECT;
            case "multiselect": return MULTI_SELECT;
            case "checkbox": return CHECKBOX;
            case "inc": return INC;
            case "url": return URL;
            case "save time": return SAVE_TIME;
            case "update time": return UPDATE_TIME;
            case "user date": return USER_DATE;
            case "user time": return USER_TIME;
            case "file": return FILE;
            case "image": return IMAGE;
            default: return null;
            }
        }

        Type(String title)
        {
            this.title = title;
        }
    }

    public enum Alignment
    {
        LEFT("left"),
        RIGHT("right"),
        CENTER("center");

        public final String title;

        public static Alignment get(String alignment)
        {
            switch (alignment)
            {
            case "left": return LEFT;
            case "right": return RIGHT;
            case "center": return CENTER;
            default: return null;
            }
        }

        Alignment(String titile)
        {
            this.title = titile;
        }
    }


    /**
     * The unique name of attribute.
     */
    @NotNull(message = "Name cannot be null")
    private String name = "";

    /**
     * The title of attribute, which will be displayed on the form.
     */
    @NotNull(message = "Title cannot be null")
    private String title = "";

    /**
     * Method responsible for aggregation operations on folders.
     */
    @NotNull(message = "Method cannot be null")
    private String method = Method.NONE.title;

    /**
     * The visibility of column with this attribute in table
     */
    @NotNull(message = "Visible cannot be null")
    private Boolean visible = DefaultValue.VISIBLE;

    /**
     * Value type.
     */
    @NotNull(message = "Type cannot be null")
    private String type = Type.TEXT.title;

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
     */
    private Double min = null;

    /**
     * For int types applies as the upper bound. For text types - as the maximum length.
     */
    private Double max = null;

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
    @NotNull(message = "Alignment cannot be null")
    private String alignment = DefaultValue.ALIGNMENT;

    /**
     * Specifies if the attribute must be filled.
     */
    @NotNull(message = "Required cannot be null")
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

    @NotNull(message = "EditableIntable cannot be null")
    private Boolean editableInTable = false;

    private String dateFormat;

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
                        + "min=%s; max=%s; defaultValue=%s; linesCount=%s; alignment=%s; regex=%s; selectOptions=%s; "
                        + "editableInTable=%s",
                name, title, method, visible, required, type,
                ofNullable(minWidth).orElse("null"), ofNullable(maxWidth).orElse("null"),
                min == null ? "null" : min.toString(), max == null ? "null" : max.toString(),
                ofNullable(defaultValue).orElse("null"), linesCount == null ? "null" : linesCount.toString(),
                ofNullable(alignment).orElse("null"), ofNullable(regex).orElse("null"), options,
                editableInTable);
    }
}
