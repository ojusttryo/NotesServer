package ru.justtry.mappers;

import static ru.justtry.shared.Constants.MONGO_ID;
import static ru.justtry.shared.ScaledImageConstants.IMAGE;
import static ru.justtry.shared.ScaledImageConstants.ORIGINAL_ID;
import static ru.justtry.shared.ScaledImageConstants.SIZE;

import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import ru.justtry.fileprocessing.ScaledImage;
import ru.justtry.shared.Identifiable;

@Component
public class ScaledImageMapper extends Mapper
{
    @Override
    public Identifiable getObject(Document document)
    {
        ScaledImage image = new ScaledImage();
        image.setId(document.get(MONGO_ID).toString());
        image.setOriginalId(document.get(ORIGINAL_ID).toString());
        image.setSize((int)document.get(SIZE));
        image.setImage(((Binary)document.get(IMAGE)).getData());
        return image;
    }


    @Override
    public Document getDocument(Identifiable object)
    {
        ScaledImage image = (ScaledImage)object;

        Document document = new Document()
                .append(ORIGINAL_ID, image.getOriginalId())
                .append(SIZE, image.getSize())
                .append(IMAGE, image.getImage());

        if (!Strings.isNullOrEmpty(image.getId()))
            document.append(MONGO_ID, new ObjectId(image.getId()));

        return document;
    }
}
