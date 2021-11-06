package ru.justtry.servlet;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.p;
import static j2html.TagCreator.text;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import j2html.tags.specialized.PTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.justtry.database.LogRecord;
import ru.justtry.rest.controllers.LogController;
import ru.justtry.shared.ResourceUtils;

/**
 * Servlet to display change log of application.
 */
@RequiredArgsConstructor
@WebServlet(name = "log", description = "Log information", urlPatterns = "/log")
@Slf4j
public class LogServlet extends HttpServlet
{
    private static final DateFormat LOG_TIME_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private final LogController logController;


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String pathInfo = request.getPathInfo();
        String pathTrimmed = StringUtils.strip(pathInfo, "/");
        if (!StringUtils.isNumeric(pathTrimmed))
            throw new IllegalArgumentException("Incorrect URL. Expected /log/[count of records]. E.g. /log/50");

        response.setContentType("text/html");
        ServletOutputStream outputStream = response.getOutputStream();

        try
        {
            String htmlTemplate = ResourceUtils.readTextFile("web/template.html");
            String div = createDivWithLogs(Integer.parseInt(pathTrimmed));
            outputStream.write(String.format(htmlTemplate, new Date().toString(), div).getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e)
        {
            // TODO if there is an error put it into the error-label
            outputStream.write(e.getMessage().getBytes(StandardCharsets.UTF_8));
        }
        finally
        {
            outputStream.close();
        }
    }

    private String createDivWithLogs(int logCount)
    {
        LogRecord[] foundLogs = logController.get(logCount).getBody();
        if (foundLogs == null || foundLogs.length == 0)
            foundLogs = new LogRecord[0];

        return div(
                each(Arrays.asList(foundLogs), this::createPWithLogRecord)
        ).withId("data").withStyle("display: grid;").render();
    }

    private PTag createPWithLogRecord(LogRecord logRecord)
    {
        String recordTime = LOG_TIME_FORMAT.format(new Date(logRecord.getTime()));
        String recordInfo = String.format("[%s] %s %s %s:",
                recordTime, logRecord.getCollection(), logRecord.getOperation(), logRecord.getId());

        switch (logRecord.getOperation())
        {
        case CREATE:
        case DELETE:
            return p(
                    text(recordInfo),
                    br(),
                    text(logRecord.getAfter().toString())
            );
        case UPDATE:
            return p(
                    text(recordInfo),
                    br(),
                    text("BEFORE: " + logRecord.getBefore().toString()),
                    br(),
                    text("AFTER: " + logRecord.getAfter().toString())
            );
        default:
            throw new IllegalArgumentException("Unknown log operation " + logRecord.getOperation());
        }
    }

    //        String url = request.getRequestURL().toString();
    //        String uri = request.getRequestURI();
    //        String scheme = request.getScheme();
    //        String serverName = request.getServerName();
    //        int portNumber = request.getServerPort();
    //        String contextPath = request.getContextPath();
    //        String servletPath = request.getServletPath();
    //        //String pathInfo = request.getPathInfo();
    //        String query = request.getQueryString();

    //        response.setContentType("text/html");
    //        //PrintWriter pw = response.getWriter();
    //        pw.print("Url: " + url + "<br/>");
    //        pw.print("Uri: " + uri + "<br/>");
    //        pw.print("Scheme: " + scheme + "<br/>");
    //        pw.print("Server Name: " + serverName + "<br/>");
    //        pw.print("Port: " + portNumber + "<br/>");
    //        pw.print("Context Path: " + contextPath + "<br/>");
    //        pw.print("Servlet Path: " + servletPath + "<br/>");
    //        pw.print("Path Info: " + pathInfo + "<br/>");
    //        pw.print("Query: " + query);

        /*
            Url: http://localhost:8765/log/50
            Uri: /log/50
            Scheme: http
            Server Name: localhost
            Port: 8765
            Context Path:
            Servlet Path: /log
            Path Info: /50
            Query: null
         */

}
