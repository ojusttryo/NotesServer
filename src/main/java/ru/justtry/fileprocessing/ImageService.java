package ru.justtry.fileprocessing;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

import javax.imageio.ImageIO;

import org.bson.Document;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.justtry.database.Database;
import ru.justtry.mappers.ScaledImageMapper;

@Service
@RequiredArgsConstructor
public class ImageService
{
    private final Database database;
    private final ScaledImageMapper imageMapper;


    public ScaledImage getImage(String id, int size) throws Exception
    {
        Document imageDoc = database.getImage(id, size);
        if (imageDoc == null)
        {
            createResizedCopies(id, size);
            imageDoc = database.getImage(id, size);
        }
        ScaledImage imageObject = (ScaledImage)imageMapper.getObject(imageDoc);
        return imageObject;
    }


    public void createResizedCopies(String originalId, int... sizes) throws Exception
    {
        Document metadata = database.getMetadata(originalId);
        if (metadata == null)
            throw new FileNotFoundException("File " + originalId + " not found");

        String contentType = metadata.get("contentType").toString();

        if (contentType != null && contentType.startsWith("image"))
        {
            String extension = contentType.replace("image/", "");
            BufferedImage image = ImageIO.read(database.getFileStream(originalId).getInputStream());

            for (int i = 0; i < sizes.length; i++)
                saveResizedCopy(image, sizes[i], originalId, extension);
        }
    }


    private String saveResizedCopy(BufferedImage image, int size, String id, String extension) throws Exception
    {
        BufferedImage bufferedImage = resize(image, size, size);
        ScaledImage scaledImage = new ScaledImage(id, size, convert(bufferedImage, extension));
        Document documentToSave = imageMapper.getDocument(scaledImage);
        return database.saveImage(documentToSave);
    }

    private BufferedImage resize(BufferedImage originalImage, int targetWidth, int targetHeight)
    {
        int height = originalImage.getHeight();
        int width = originalImage.getWidth();
        int min = Math.min(height, width);
        int max = Math.max(height, width);

        // Crop to make square
        if (min > targetHeight && min > targetWidth)
        {
            int indent = (max - min) / 2;

            originalImage = originalImage.getSubimage(
                    width > height ? indent : 0,
                    width > height ? 0 : indent,
                    min,
                    min);
        }

        return Scalr.resize(originalImage,
                Method.ULTRA_QUALITY,
                Mode.FIT_TO_HEIGHT,
                targetWidth,
                targetHeight,
                Scalr.OP_ANTIALIAS);
    }

    private byte[] convert(BufferedImage image, String extension) throws Exception
    {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream())
        {
            ImageIO.write(image, extension, os);
            return os.toByteArray();
        }
    }
}
