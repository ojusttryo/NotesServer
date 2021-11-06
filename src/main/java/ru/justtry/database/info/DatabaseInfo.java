package ru.justtry.database.info;

import lombok.Data;

/**
 * Information about the database
 */
@Data
public class DatabaseInfo
{
    // TODO specify types of fields

    private String name;
    private Object collections; // TODO rename to collectionsCount
    private Object dataSize;
    private Object storageSize;
}
