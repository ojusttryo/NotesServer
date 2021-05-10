package ru.justtry.metainfo.dictionary;

import static java.util.function.UnaryOperator.identity;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ImageSize
{
    SIZE_50(50),
    SIZE_100(100),
    SIZE_200(200);

    public final int size;

    private static Map<Integer, ImageSize> AVAILABLE_VALUES = Arrays.stream(values())
            .collect(Collectors.toMap(x -> x.size, identity()));;

    public static ImageSize get(int size)
    {
        return AVAILABLE_VALUES.get(size);
    }

    ImageSize(int size)
    {
        this.size = size;
    }
}
