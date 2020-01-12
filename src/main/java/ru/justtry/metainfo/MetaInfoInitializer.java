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
        AttributesController attributesController = (AttributesController)context.getBean("attributesController");

        Object[] attributes = database.getObjects(attributesController.getCollectionName(),
            attributesController.getMapper());

        if (!containsAttribute(attributes, PredefinedAttributes.NAME))
            attributesController.save(getAttributeName());

        if (!containsAttribute(attributes, PredefinedAttributes.STATE))
            attributesController.save(getAttributeState());

        if (!containsAttribute(attributes, PredefinedAttributes.COMMENT))
            attributesController.save(getAttributeComment());
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
        attribute.setName(PredefinedAttributes.NAME);
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
        attribute.setName(PredefinedAttributes.STATE);
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
        attribute.setName(PredefinedAttributes.COMMENT);
        attribute.setRequired(false);
        attribute.setType(Type.TEXTAREA);
        attribute.setLinesCount(3);
        return attribute;
    }
}