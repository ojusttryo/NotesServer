package ru.justtry.database.info;

import lombok.Data;

/**
 * Information about collection in database
 */
@Data
public class CollectionInfo
{
    private String entityName;
    private String entityTitle;
    private Object count;
    private Object size;
    private Object storageSize;
}
