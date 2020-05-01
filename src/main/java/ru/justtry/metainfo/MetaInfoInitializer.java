package ru.justtry.metainfo;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import ru.justtry.database.Database;
import ru.justtry.rest.AttributesController;
import ru.justtry.shared.AttributeConstants.PredefinedAttributes;
import ru.justtry.shared.AttributeConstants.Type;

/**
 * Creates and saves default attributes for all entities.
 */
@Component
@DependsOn({"database"})
public class MetaInfoInitializer
{
//    @Inject
//    private static AttributesController attributesController;
//    @Inject
//    @Named("database")
//    private static Database database;
    @Autowired
    private ApplicationContext context;

    @PostConstruct
    public void createDefaultMetaInfo()
    {
        Database database = (Database)context.getBean("database");
        AttributesController attrController = (AttributesController)context.getBean("attributesController");

        Object[] attrs = database.getObjects(attrController.getCollectionName(), attrController.getMapper(), null);

        if (!containsAttribute(attrs, PredefinedAttributes.NAME))
            attrController.save(getAttributeName());

        if (!containsAttribute(attrs, PredefinedAttributes.STATE))
            attrController.save(getAttributeState());

        if (!containsAttribute(attrs, PredefinedAttributes.COMMENT))
            attrController.save(getAttributeComment());
    }

    /**
     * Check if the array contains specific attribute
     * Not the best for performance. But for now it's ok. Besides, there are not many attributes.
     * @param attributes attributes to search in
     * @param attributeName the name of the attribute to search
     * @return true if contains
     */
    private boolean containsAttribute(Object[] attributes, String attributeName)
    {
        return Arrays.stream(attributes).anyMatch(x -> ((Attribute)x).getName().equals(attributeName));
    }

    private Attribute getAttributeName()
    {
        Attribute attribute = new Attribute();
        attribute.setName("name");
        attribute.setTitle("Name");
        attribute.setRequired(true);
        attribute.setVisible(true);
        return attribute;
    }

    private Attribute getAttributeState()
    {
        Attribute attribute = new Attribute();
        attribute.setVisible(true);
        attribute.setType(Type.SELECT);
        attribute.setRequired(true);
        attribute.setName("state");
        attribute.setTitle("State");
        attribute.setSelectValues("Active", "Deleted", "Postponed", "waiting", "Finished", "Not defined");
        attribute.setDefaultValue("Not selected");
        return attribute;
    }

    private Attribute getAttributeFolder()
    {
        return null;
    }

    private Attribute getAttributeComment()
    {
        Attribute attribute = new Attribute();
        attribute.setVisible(false);
        attribute.setName("comment");
        attribute.setTitle("Comment");
        attribute.setRequired(false);
        attribute.setType(Type.TEXTAREA);
        attribute.setLinesCount(3);
        return attribute;
    }
}
