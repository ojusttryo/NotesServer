package ru.justtry.database.sort;

import static java.util.function.UnaryOperator.identity;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Direction
{
    ASCENDING("ascending"),
    DESCENDING("descending");

    public final String title;

    private static final Map<String, Direction> AVAILABLE_VALUES = Arrays.stream(values())
        .collect(Collectors.toMap(x -> x.title, identity()));

    @Nullable
    public static Direction get(String title)
    {
        return AVAILABLE_VALUES.getOrDefault(title, null);
    }

}