package ru.justtry.shared;

public abstract class ErrorMessages
{
    public static String NAME_IS_DUPLICATED = "Attribute with this name is already exists";
    public static String NOT_ALL_ATTRIBUTES_FOUND = "Some attributes are not found";
    public static String NOT_ALL_COMPARED_ATTRIBUTES_FOUND = "Some compared attributes doesn't exist";
    public static String NOT_ALL_VISIBLE_ATTRIBUTES_FOUND = "Some visibled attributes doesn't exist";


    public static String getIsNotSet(String attribute)
    {
        return attribute + " is not set";
    }

    public static String getShouldBeInteger(String attribute)
    {
        return attribute + " should be integer";
    }

    public static String getShouldBeNumber(String attribute)
    {
        return attribute + " should be numeric type (integer or double)";
    }

    public static String getIsNotInPredefinedValues(String attribute)
    {
        return attribute + " is not one of the predefined values";
    }
}
