package ru.justtry.database.info;

import lombok.Data;

/**
 * Information about the database
 */
@Data
public class DatabaseInfo
{
    private String name;
    private Object collections;
    private Object dataSize;
    private Object storageSize;
}
