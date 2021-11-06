package ru.justtry.servlet;

import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.span;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import j2html.tags.DomContent;
import j2html.tags.specialized.SpanTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.justtry.database.info.CollectionInfo;
import ru.justtry.database.info.DatabaseInfo;
import ru.justtry.database.info.FilesInfo;
import ru.justtry.rest.controllers.StatisticsController;
import ru.justtry.shared.ResourceUtils;

/**
 * Servlet to display statistics about database and notes.
 */
@RequiredArgsConstructor
@WebServlet(name = "statistics", description = "Statistical information", urlPatterns = "/statistics")
@Slf4j
public class StatisticsServlet extends HttpServlet
{
    private static final String DATA_TABLE_STYLE = ""
        + "min-width: 100%; "
        + "grid-template-columns: 1fr 1fr 1fr 1fr 1fr; "
        + "padding: 10px; ";
    private static final String HEADER_STYLE = ""
        + "justify-self: center; "
        + "text-align: center; "
        + "font-weight: bold; "
        + "grid-column: 1 / 6; ";
    private static final String JUSTIFY_SELF_RIGHT = "justify-self: right; ";
    private static final String JUSTIFY_SELF_LEFT = "justify-self: left; ";
    private static final String TEXT_ALIGN_LEFT = "text-align: left; ";
    private static final String TEXT_ALIGN_RIGHT = "text-align: right; ";
    private static final String BOLD = "font-weight: bold; ";

    private final StatisticsController statisticsController;


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.setContentType("text/html");
        ServletOutputStream outputStream = response.getOutputStream();

        try
        {
            String htmlTemplate = ResourceUtils.readTextFile("web/template.html");
            String div = createDivWithStatistics();
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


    private String createDivWithStatistics()
    {
        CollectionInfo[] collectionInfo = statisticsController.getNotesInfo().getBody();
        FilesInfo[] filesInfo = statisticsController.getFilesInfo().getBody();
        DatabaseInfo databaseInfo = statisticsController.getDatabaseInfo().getBody();
        FilesInfo iconsInfo = statisticsController.getIconsInfo().getBody();

        if (collectionInfo == null || filesInfo == null || databaseInfo == null || iconsInfo == null)
            throw new IllegalStateException("Error while loading statistics information");

        // TODO place each section into a separate function
        return div(
            div(
                span("Database information").withStyle(HEADER_STYLE),

                // TODO remove these ugly spans, replace with style tags
                each(Collections.nCopies(10, span()).toArray(new SpanTag[10])),

                span(),
                span("Name").withStyle(JUSTIFY_SELF_LEFT + TEXT_ALIGN_LEFT + BOLD),
                span("Collections count").withStyle(JUSTIFY_SELF_RIGHT + TEXT_ALIGN_RIGHT + BOLD),
                span("Total Size, Mb").withStyle(JUSTIFY_SELF_RIGHT + TEXT_ALIGN_RIGHT + BOLD),
                span("Storage size, Mb").withStyle(JUSTIFY_SELF_RIGHT + TEXT_ALIGN_RIGHT + BOLD),

                span(),
                span(databaseInfo.getName()).withStyle(JUSTIFY_SELF_LEFT + TEXT_ALIGN_LEFT),
                span(databaseInfo.getCollections().toString()).withStyle(JUSTIFY_SELF_RIGHT + TEXT_ALIGN_RIGHT),
                span(getDataSizeMb(databaseInfo)).withStyle(JUSTIFY_SELF_RIGHT + TEXT_ALIGN_RIGHT),
                span(getStorageSizeMb(databaseInfo)).withStyle(JUSTIFY_SELF_RIGHT + TEXT_ALIGN_RIGHT),

                each(Collections.nCopies(20, span()).toArray(new SpanTag[20])),

                span("Files information").withStyle(HEADER_STYLE),

                each(Collections.nCopies(10, span()).toArray(new SpanTag[10])),

                span("File type").withClass("twoCols").withStyle(JUSTIFY_SELF_LEFT + TEXT_ALIGN_LEFT + BOLD),
                span("Files count").withStyle(JUSTIFY_SELF_RIGHT + TEXT_ALIGN_RIGHT + BOLD),
                span("Total size, Kb").withStyle(JUSTIFY_SELF_RIGHT + TEXT_ALIGN_RIGHT + BOLD),
                span(),

                each(Arrays.asList(filesInfo), this::getFileInfoRow),
                getFileInfoRow(iconsInfo),

                span("Notes information").withStyle(HEADER_STYLE),

                each(Collections.nCopies(10, span()).toArray(new SpanTag[10])),

                span("Title").withStyle(JUSTIFY_SELF_LEFT + TEXT_ALIGN_LEFT + BOLD),
                span("Name").withStyle(JUSTIFY_SELF_LEFT + TEXT_ALIGN_LEFT + BOLD),
                span("Count of notes").withStyle(JUSTIFY_SELF_RIGHT + TEXT_ALIGN_RIGHT + BOLD),
                span("Total size, B").withStyle(JUSTIFY_SELF_RIGHT + TEXT_ALIGN_RIGHT + BOLD),
                span("Storage size, B").withStyle(JUSTIFY_SELF_RIGHT + TEXT_ALIGN_RIGHT + BOLD),

                each(Arrays.asList(collectionInfo), this::getCollectionInfoRow)

            ).withClass("data-table").withStyle(DATA_TABLE_STYLE)
        ).withId("data").withStyle("display: grid;").render();
    }

    private String getDataSizeMb(DatabaseInfo databaseInfo) {
        double size = (Double)databaseInfo.getDataSize() / 1_000_000;
        return formatDouble(size);
    }

    private String getStorageSizeMb(DatabaseInfo databaseInfo) {
        double size = (Double)databaseInfo.getStorageSize() / 1_000_000;
        return formatDouble(size);
    }

    private DomContent getFileInfoRow(FilesInfo filesInfo) {

        double size = Double.parseDouble(filesInfo.getSize().toString()) / 1_000;

        return each(
            span(filesInfo.getContentType()).withClass("twoCols"),
            span(filesInfo.getCount().toString()).withStyle(JUSTIFY_SELF_RIGHT + TEXT_ALIGN_RIGHT),
            span(formatDouble(size)).withStyle(JUSTIFY_SELF_RIGHT + TEXT_ALIGN_RIGHT)
        );
    }

    private DomContent getCollectionInfoRow(CollectionInfo collectionInfo) {
        double size = Double.parseDouble(collectionInfo.getSize().toString());
        double storageSize = Double.parseDouble(collectionInfo.getStorageSize().toString());

        return each(
            span(collectionInfo.getEntityTitle()).withStyle(JUSTIFY_SELF_LEFT + TEXT_ALIGN_LEFT),
            span(collectionInfo.getEntityName()).withStyle(JUSTIFY_SELF_LEFT + TEXT_ALIGN_LEFT),
            span(collectionInfo.getCount().toString()).withStyle(JUSTIFY_SELF_RIGHT + TEXT_ALIGN_RIGHT),
            span(formatDouble(size)).withStyle(JUSTIFY_SELF_RIGHT + TEXT_ALIGN_RIGHT),
            span(formatDouble(storageSize)).withStyle(JUSTIFY_SELF_RIGHT + TEXT_ALIGN_RIGHT)
        );
    }


    /**
     * Separates a double number with spaces after every three digits and rounds it.
     * @param value the number to be formatted
     * @return result of operations
     */
    private String formatDouble(double value) {
        String formatted = BigDecimal.valueOf(value).setScale(0, RoundingMode.UP).toString();
        formatted = new StringBuilder(formatted).reverse().toString();
        formatted = formatted.replaceAll("(.{3})", "$1 ");
        formatted = new StringBuilder(formatted).reverse().toString();
        return formatted;
    }

}
