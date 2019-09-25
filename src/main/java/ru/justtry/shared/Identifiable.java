package ru.justtry.shared;

/**
 * The entity which has identifier.
 */
public abstract class Identifiable
{
    /**
     * The identifier of the object used to distinguish objects in database.
     */
    private String id;

    /**
     * Get the object identifier.
     * @return {@link #id object identifier}
     */
    public String getId()
    {
        return id;
    }

    /**
     * Set the {@link #id object identifier}.
     * @param id identifier
     */
    public void setId(String id)
    {
        this.id = id;
    }
}
