package ru.justtry.mappers;

import static ru.justtry.shared.Constants.MONGO_ID;
import static ru.justtry.shared.EntityConstants.*;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import ru.justtry.metainfo.Entity;
import ru.justtry.shared.Identifiable;

@Component
public class EntityMapper extends Mapper
{
    @Override
    public Identifiable getObject(Document document)
    {
        Entity entity = new Entity();
        entity.setId(document.get(MONGO_ID).toString());
        entity.setName(document.get(NAME).toString());
        entity.setTitle(document.get(TITLE).toString());
        entity.setVisible((boolean)document.get(VISIBLE));
        entity.setKeyAttribute(document.get(KEY_ATTRIBUTE).toString());
        entity.setSortAttribute(getStringOrNull(document, SORT_ATTRIBUTE));
        entity.setSortDirection(getStringOrNull(document, SORT_DIRECTION));
        entity.setAttributes((List<String>)document.get(ATTRIBUTES));
        entity.setComparedAttributes((List<String>)document.get(COMPARED_ATTRIBUTES));
        entity.setVisibleAttributes((List<String>)document.get(VISIBLE_ATTRIBUTES));

        return entity;
    }


    @Override
    public Document getDocument(Identifiable object)
    {
        Entity entity = (Entity)object;

        Document document = new Document()
                .append(NAME, entity.getName())
                .append(TITLE, entity.getTitle())
                .append(VISIBLE, entity.isVisible())
                .append(KEY_ATTRIBUTE, entity.getKeyAttribute())
                .append(SORT_ATTRIBUTE, entity.getSortAttribute())
                .append(SORT_DIRECTION, entity.getSortDirection())
                .append(ATTRIBUTES, entity.getAttributes())
                .append(COMPARED_ATTRIBUTES, entity.getComparedAttributes())
                .append(VISIBLE_ATTRIBUTES, entity.getVisibleAttributes());

        if (!Strings.isNullOrEmpty(entity.getId()))
            document.append(MONGO_ID, new ObjectId(entity.getId()));

        return document;
    }
}
