package ru.justtry.shared;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

import lombok.experimental.UtilityClass;

/**
 * The utility class to work with resource files.
 */
@UtilityClass
public class ResourceUtils
{

    /**
     * Read a text file from resources.
     *
     * @param path relative path after resources/
     * @return a text file as a string in UTF-8
     * @throws IOException in case of error in copying file to result string
     */
    public static String readTextFile(String path) throws IOException
    {
        InputStream inputStream = readStream(path);
        // or IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(inputStream, stringWriter, "UTF-8");
        return stringWriter.toString();
    }


    /**
     * Reads the specified resource file into {@link InputStream}.
     * @param name a name of the file relative to the source folder, e.g. 'filename.xml'
     * @return open {@link InputStream}
     * @throws IllegalStateException in case the resource cannot be opened
     */
    public static InputStream readStream(String name)
    {
        InputStream is = ResourceUtils.class.getClassLoader().getResourceAsStream(name);
        if (is == null)
            throw new IllegalStateException("Cannot read resource file " + name);
        return is;
    }

}
