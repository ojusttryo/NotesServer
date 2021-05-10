package ru.justtry.metainfo.dictionary;

import static java.util.function.UnaryOperator.identity;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum Alignment
{
    LEFT("left"),
    RIGHT("right"),
    CENTER("center");

    public final String title;

    private static Map<String, Alignment> AVAILABLE_VALUES = Arrays.stream(values())
            .collect(Collectors.toMap(x -> x.title, identity()));;

    public static Alignment get(String alignment)
    {
        return AVAILABLE_VALUES.get(alignment);
    }

    Alignment(String title)
    {
        this.title = title;
    }
}