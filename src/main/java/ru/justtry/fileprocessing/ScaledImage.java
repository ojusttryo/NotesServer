package ru.justtry.fileprocessing;

import lombok.Data;
import ru.justtry.shared.Identifiable;

@Data
public class ScaledImage extends Identifiable
{
    private String originalId;
    private int size;
    //@JsonProperty("image")
    private byte[] image;

    public ScaledImage()
    {

    }

    public ScaledImage(String originalId, int size, byte[] image)
    {
        this.originalId = originalId;
        this.size = size;
        this.image = image;
    }

//    @JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ByteArraySerializer.class)
//    public byte[] getImage()
//    {
//        return image;
//    }
}
