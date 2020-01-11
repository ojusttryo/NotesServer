package ru.justtry.shared;

public interface AttributeConstants
{
    String ATTRIBUTES_COLLECTION = "attributes";
    String METHOD = "method";
    String VISIBLE = "visible";
    String TYPE = "type";
    String MIN_WIDTH = "minWidth";
    String MAX_WIDTH = "maxWidth";
    String MIN = "min";
    String MAX = "max";
    String DEFAULT = "default";
    String LINES_COUNT = "linesCount";
    String ALIGNMENT = "alignment";
    String LEFT = "left";
    String REQUIRED = "required";
    String REGEX = "regex";
    String INT = "int";
    String FLOAT = "float";

    interface Type
    {
        String TEXT = "text";
        String TEXTAREA = "textarea";
        String SELECT = "select";
    }

    interface Method
    {
        String NONE = "none";
        String FOLDER_NAME = "folder name";
        String AVG = "avg";
        String COUNT = "count";
    }

    interface DefaultValue
    {
        Integer WIDTH = 0;
        Integer LINES_COUNT = 1;
        String ALIGNMENT = LEFT;
        boolean REQUIRED = false;
        boolean VISIBLE = true;
        String TYPE = Type.TEXT;
    }

    interface PredefinedAttributes
    {
        String NAME = "Name";
        String ADD_TIME = "Add time";
        String UPDATE_TIME = "Last update time";
        String STATE = "State";
        String FOLDER = "Folder";
        String COMMENT = "Comment";
    }
}
