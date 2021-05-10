package ru.justtry.metainfo.dictionary;

import static java.util.function.UnaryOperator.identity;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum Type
{
    TEXT("text"),                       // single line string
    TEXT_AREA("textarea"),              // multiline string
    DELIMITED_TEXT("delimited text"),   // string with delimited like ;
    NUMBER("number"),                   // numeric field (double or integer)
    SELECT("select"),                   // drop-down list with single selected value, represents enum value
    MULTI_SELECT("multiselect"),        // drop-down list that allows multiple selections
    CHECKBOX("checkbox"),               // boolean value
    INC("inc"),                         // incremented number, which has plus sign beside
    URL("url"),                         // the URL
    SAVE_TIME("save time"),             // timestamp when the note is saved
    UPDATE_TIME("update time"),         // timestamp when the note is updated
    USER_DATE("user date"),             // custom date (yyyy-mm-dd)
    USER_TIME("user time"),             // custom time (hh:mm, 24-hours format)
    FILE("file"),
    IMAGE("image"),
    FILES("files"),
    GALLERY("gallery"),
    NESTED_NOTES("nested notes"),
    RELATED_NOTES("related notes"),
    COMPARED_NOTES("compared notes"),
    ROW_NUMBER("row number");

    public final String title;

    private static final Map<String, Type> AVAILABLE_VALUES = Arrays.stream(values())
            .collect(Collectors.toMap(x -> x.title, identity()));


    public static boolean isTextType(Type type)
    {
        return (type == TEXT || type == TEXT_AREA || type == DELIMITED_TEXT);
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

    public static boolean isTimestampType(String type)
    {
        Type t = get(type);
        return (t != null) && isTimestampType(t);
    }

    public static boolean isFile(Type type)
    {
        return (type == FILE || type == IMAGE);
    }

    public static boolean isMultiFile(Type type)
    {
        return (type == GALLERY || type == FILES);
    }

    public static boolean isNotesList(Type type)
    {
        return (type == NESTED_NOTES || type == RELATED_NOTES || type == COMPARED_NOTES);
    }

    public static Type get(String type)
    {
        return AVAILABLE_VALUES.get(type);
    }

    Type(String title)
    {
        this.title = title;
    }
}