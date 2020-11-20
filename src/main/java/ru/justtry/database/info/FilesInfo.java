package ru.justtry.database.info;

import lombok.Data;

/**
 * Information about a single file type in database.
 */
@Data
public class FilesInfo
{
    private String contentType;
    private Object count;
    private Object size;
}
