package ru.justtry.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.justtry.shared.ResourceUtils;

/**
 * The HttpServlet for getting resource files from application to a browser via additional requests.
 */
@RequiredArgsConstructor
@WebServlet(name = "resources", description = "Resources files servlet", urlPatterns = "/resources")
@MultipartConfig
@Slf4j
public class ResourceServlet extends HttpServlet
{

    private final Map<String, String> CACHED_RESOURCES = new HashMap<>();


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String pathInfo = request.getPathInfo();
        String pathTrimmed = StringUtils.strip(pathInfo, "/");

        PrintWriter pw = response.getWriter();
        if (CACHED_RESOURCES.containsKey(pathTrimmed))
        {
            pw.write(CACHED_RESOURCES.get(pathTrimmed));
        }
        else
        {
            String file = ResourceUtils.readTextFile("web/" + pathTrimmed);
            pw.write(file);
            CACHED_RESOURCES.put(pathTrimmed, file);
        }

        pw.close();
    }

}
