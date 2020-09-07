package ru.justtry.shared;

public interface AttributeConstants
{
    String NAME = "name";
    String TITLE = "title";
    String ATTRIBUTES_COLLECTION = "attributes";
    String METHOD = "method";
    String VISIBLE = "visible";
    String TYPE = "type";
    String SELECT_OPTIONS = "selectOptions";
    String MIN_WIDTH = "minWidth";
    String MAX_WIDTH = "maxWidth";
    String MIN_HEIGHT = "minHeight";
    String MAX_HEIGHT = "maxHeight";
    String MIN = "min";
    String MAX = "max";
    String DEFAULT = "default";
    String STEP = "step";
    String LINES_COUNT = "linesCount";
    String ALIGNMENT = "alignment";
    String IMAGES_SIZE = "imagesSize";
    String LEFT = "left";
    String REQUIRED = "required";
    String REGEX = "regex";
    String DELIMITER = "delimiter";
    String EDITABLE_IN_TABLE = "editableInTable";
    String DATE_FORMAT = "dateFormat";

    interface Type
    {
        String TEXT = "text";
        String TEXTAREA = "textarea";
        String SELECT = "select";
    }

    interface DefaultValue
    {
        Integer WIDTH = 0;
        Integer LINES_COUNT = 1;
        Integer STEP = 1;
        String ALIGNMENT = LEFT;
        boolean REQUIRED = false;
        boolean VISIBLE = true;
    }

    interface PredefinedAttributes
    {
        String NAME = "name";
        String ADD_TIME = "add time";
        String UPDATE_TIME = "last update time";
        String STATE = "state";
        String FOLDER = "folder";
        String COMMENT = "comment";
    }
}
