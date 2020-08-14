package ru.justtry.fileprocessing;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.springframework.stereotype.Component;

@Component
public class ImageEditor
{
    public BufferedImage resize(BufferedImage originalImage, int targetWidth, int targetHeight)
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

    public byte[] convert(BufferedImage image, String extension) throws Exception
    {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream())
        {
            ImageIO.write(image, extension, os);
            return os.toByteArray();
        }
    }
}
