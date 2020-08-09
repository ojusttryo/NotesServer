package ru.justtry.mappers;

import static ru.justtry.shared.AttributeConstants.*;
import static ru.justtry.shared.Constants.MONGO_ID;
import static ru.justtry.shared.Constants.NAME;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import ru.justtry.metainfo.Attribute;
import ru.justtry.shared.Identifiable;

@Component
public class AttributeMapper extends Mapper
{
    @Override
    public Identifiable getObject(Document document)
    {
        Attribute attribute = new Attribute();

        attribute.setId(document.get(MONGO_ID).toString());
        attribute.setName(document.get(NAME).toString());
        attribute.setTitle(document.get(TITLE).toString());
        attribute.setAlignment(document.get(ALIGNMENT).toString());
        attribute.setLinesCount((Integer)document.get(LINES_COUNT));
        attribute.setType(document.get(TYPE).toString());
        attribute.setSelectOptions((List<String>)document.get(SELECT_OPTIONS));
        attribute.setVisible((Boolean)document.get(VISIBLE));
        attribute.setMethod(getStringOrNull(document, METHOD));
        attribute.setMaxWidth(getStringOrNull(document, MAX_WIDTH));
        attribute.setMinWidth(getStringOrNull(document, MIN_WIDTH));
        attribute.setMin((Double)document.get(MIN));
        attribute.setMax((Double)document.get(MAX));
        attribute.setDefaultValue(getStringOrNull(document, DEFAULT));
        attribute.setStep(getStringOrNull(document, STEP));
        attribute.setRequired((Boolean)document.get(REQUIRED));
        attribute.setRegex(getStringOrNull(document, REGEX));
        attribute.setEditableInTable((Boolean)document.get(EDITABLE_IN_TABLE));
        attribute.setDateFormat(getStringOrNull(document, DATE_FORMAT));

        return attribute;
    }


    @Override
    public Identifiable[] getObjects(List<Document> documents)
    {
        List<Identifiable> objects = new ArrayList<>();
        for (Document document : documents)
            objects.add(getObject(document));
        return objects.toArray(new Identifiable[0]);
    }


    @Override
    public Document getDocument(Identifiable object)
    {
        Attribute attribute = (Attribute)object;

        Document document = new Document()
                .append(NAME, attribute.getName())
                .append(TITLE, attribute.getTitle())
                .append(METHOD, attribute.getMethod())
                .append(VISIBLE, attribute.getVisible())
                .append(TYPE, attribute.getType())
                .append(MIN_WIDTH, attribute.getMinWidth())
                .append(MAX_WIDTH, attribute.getMaxWidth())
                .append(LINES_COUNT, attribute.getLinesCount())
                .append(ALIGNMENT, attribute.getAlignment())
                .append(DEFAULT, attribute.getDefaultValue())
                .append(STEP, attribute.getStep())
                .append(MAX, attribute.getMax())
                .append(MIN, attribute.getMin())
                .append(REQUIRED, attribute.getRequired())
                .append(REGEX, attribute.getRegex())
                .append(EDITABLE_IN_TABLE, attribute.getEditableInTable())
                .append(DATE_FORMAT, attribute.getDateFormat())
                .append(SELECT_OPTIONS, attribute.getSelectOptions());

        if (!Strings.isNullOrEmpty(attribute.getId()))
            document.append(MONGO_ID, new ObjectId(attribute.getId()));

        return document;
    }
}
