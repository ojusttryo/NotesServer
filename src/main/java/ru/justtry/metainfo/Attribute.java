package ru.justtry.metainfo;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import ru.justtry.metainfo.dictionary.Alignment;
import ru.justtry.metainfo.dictionary.ImageSize;
import ru.justtry.metainfo.dictionary.Method;
import ru.justtry.metainfo.dictionary.Type;
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

    /** The unique name of attribute */
    @NotNull(message = "Name should be set")
    private String name = "";

    /** The title of attribute, which will be displayed on the form */
    @NotNull(message = "Title should be set")
    private String title = "";

    /** Method responsible for aggregation operations */
    @NotNull(message = "Method cannot be null")
    private String method = Method.NONE.title;

    /** Value type */
    @NotNull(message = "Type cannot be null")
    private String type = Type.TEXT.title;

    /**
     * Minimal width of the field with this attribute or the input at form (like gallery).
     * It can stores values like "200" and "20em"
     */
    private String minWidth = null;

    /**
     * Maximum width of the field with this attribute  or the while input at form (like gallery).
     * Could keep value like "200" and "20em".
     */
    private String maxWidth = null;

    /**
     * Minimum height of input at form (like gallery).
     * Could keep value like "200" and "20em"
     */
    private String minHeight = null;

    /**
     * Maximum height of input at form (like gallery).
     * Could keep value like "200" and "20em"
     */
    private String maxHeight = null;

    /**
     * For number types applies as the lower bound.
     * For text types - as the minimum length.
     * For files - as the minimum size (Kb)
     */
    private Double min = null;

    /**
     * For numeric types applies as the upper bound.
     * For text types - as the maximum length.
     * For files - as the maximum size (Kb)
     */
    private Double max = null;

    /**
     * Default value of attribute.
     */
    private String defaultValue = null;

    /**
     * Step of numeric value. Keep in string for precision.
     */
    private String step = "1";

    /**
     * Lines count to display this attribute.
     * Except textarea the value is ignored.
     */
    private Integer linesCount = 1;

    private Integer imagesSize = ImageSize.SIZE_100.size;

    /**
     * The alignment of the attribute in data table.
     */
    @NotNull(message = "Alignment cannot be null")
    private String alignment = Alignment.LEFT.getTitle();

    /**
     * Specifies if the attribute must be filled.
     */
    @NotNull(message = "Required cannot be null")
    private Boolean required = false;

    /**
     * The regular expression is used to check the correctness of value.
     */
    private String regex;

    private String delimiter = null;

    private String entity = null;

    /**
     * The values of drop-down list (enum)
     */
    @Setter(AccessLevel.NONE)
    private List<String> selectOptions;

    @NotNull(message = "EditableInTable cannot be null")
    private Boolean editableInTable = false;

    private String dateFormat;

    /**
     * Defines whether attribute should be visible for another entities. It means that after first addition
     * this attributes won't be shown in the list of available attributes on entity form.
     */
    private boolean shared = true;

    private List<String> usage = new ArrayList<>();

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


    public Type getTypeAsEnum()
    {
        Type t = Type.get(this.getType());
        if (t == null)
            throw new IllegalStateException("Current type is not one of the specified types");
        return t;
    }


    @Override
    public String toString()
    {
        String options = selectOptions == null ? "null" :
                Stream.of(selectOptions).map(Object::toString).collect(Collectors.joining(", "));

        return String.format("%s (title=%s; method=%s; required=%s; type=%s; minWidth=%s; maxWidth=%s, "
                        + "min=%s; max=%s; defaultValue=%s; linesCount=%s; alignment=%s; regex=%s; selectOptions=%s; "
                        + "editableInTable=%s",
                name, title, method, required, type,
                ofNullable(minWidth).orElse("null"), ofNullable(maxWidth).orElse("null"),
                min == null ? "null" : min.toString(), max == null ? "null" : max.toString(),
                ofNullable(defaultValue).orElse("null"), linesCount == null ? "null" : linesCount.toString(),
                ofNullable(alignment).orElse("null"), ofNullable(regex).orElse("null"), options,
                editableInTable);
    }
}
