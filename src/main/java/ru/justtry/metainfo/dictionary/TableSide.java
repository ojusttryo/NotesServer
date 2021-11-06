package ru.justtry.metainfo.dictionary;

import static java.util.function.UnaryOperator.identity;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The side of an attributes table on an entity form.
 */
@Getter
@RequiredArgsConstructor
public enum TableSide
{
    LEFT("left"),
    RIGHT("right");

    private final String value;

    private static final Map<String, TableSide> AVAILABLE_VALUES = Arrays.stream(values())
        .collect(Collectors.toMap(x -> x.value, identity()));;

    public static TableSide get(String alignment)
    {
        return AVAILABLE_VALUES.get(alignment);
    }

    public TableSide getOpposite()
    {
        return this == LEFT ? RIGHT : LEFT;
    }

}
