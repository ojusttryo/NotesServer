package ru.justtry.rest.errors;

import java.util.Date;

import lombok.Data;

@Data
public class RestError
{
    private String message;
    private long time = new Date().getTime();

    public RestError()
    {

    }

    public RestError(String message)
    {
        this.message = message;
    }
}
