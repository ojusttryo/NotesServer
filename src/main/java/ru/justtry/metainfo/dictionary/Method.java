package ru.justtry.metainfo.dictionary;

import static java.util.function.UnaryOperator.identity;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum Method
{
    NONE("none"),                   // does nothing, just empty field
    AVG("avg"),
    SUM("sum"),
    MIN("min"),
    MAX("max"),
    RANGE("range"),                 // range of values like "5 - 200"
    EMPTY("empty"),                 // count of empty elements
    COUNT("count");

    public final String title;

    private static final Map<String, Method> AVAILABLE_VALUES = Arrays.stream(values())
            .collect(Collectors.toMap(x -> x.title, identity()));

    public static Method get(String method)
    {
        return AVAILABLE_VALUES.get(method);
    }

    Method(String title)
    {
        this.title = title;
    }
}