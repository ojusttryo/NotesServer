package ru.justtry.servlet;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.input;
import static j2html.TagCreator.span;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import j2html.tags.DomContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.justtry.metainfo.Entity;
import ru.justtry.rest.controllers.EntitiesController;
import ru.justtry.shared.ResourceUtils;

/**
 * Servlet to display information about existing entities.
 */
@RequiredArgsConstructor
@WebServlet(name = "entities", description = "Entities information", urlPatterns = "/entities")
@Slf4j
public class EntitiesServlet extends HttpServlet
{
    private final EntitiesController entitiesController;


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.setContentType("text/html");
        ServletOutputStream outputStream = response.getOutputStream();

        try
        {
            String htmlTemplate = ResourceUtils.readTextFile("web/template.html");
            String div = createContent().render();
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

    private DomContent createContent()
    {
        return each(createDataMenu(), createDataTable());
    }

    private DomContent createDataMenu()
    {
        return div(
            input().withId("add-entity-button").withType("button").withValue("New entity")
        ).withId("data-menu").withClass("data-menu").withStyle("display: flex");
    }

    private DomContent createDataTable()
    {
        return div(createTableHeader(), createTableContent())
            .withId("data-table")
            .withClasses("data-table", "has-vertical-padding")
            .withStyle("grid-template-columns: min-content repeat(4, auto) min-content min-content; display: grid;");
    }

    private DomContent createTableHeader()
    {
        return each(
            span("№").withStyle("justify-self: center; text-align: center; font-weight: bold;"),
            span("Title").withStyle("font-weight: bold;"),
            span("Name").withStyle("font-weight: bold;"),
            span("Visible").withStyle("font-weight: bold;"),
            span("Attributes").withStyle("font-weight: bold;"),
            span(),
            span()
        );
    }

    private DomContent createTableContent()
    {
        Entity[] entities = entitiesController.get();
        return each(Arrays.asList(entities), this::createTableRow);
    }

    private DomContent createTableRow(Integer index, Entity entity)
    {
        return each(
            span(String.valueOf(index + 1)).withStyle("justify-self: right; text-align: right;"),
            span(entity.getTitle()),
            span(entity.getName()),
            span(Boolean.toString(entity.isVisible())),
            createAttributeLinks(entity),
            span()
                .withClass("editButton")
                .attr("content-name", entity.getName())
                .attr("href", "/entities/entity/" + entity.getName())
                .attr("onclick", "function() { window.location.replace(this.href) };"),
            span()
                .withClass("deleteButton")
                .attr("content-name", entity.getName())
                .attr("href", "/entities/entity/" + entity.getName())
        );
    }

    private DomContent createAttributeLinks(Entity entity)
    {
        return span(
            each(entity.getAttributes(), this::createAttributeLink)
        ).withClass("usage-list").withStyle("display: flex;");
    }

    private DomContent createAttributeLink(String attributeName)
    {
        return a(attributeName)
            .withHref("/attributes/attribute/" + attributeName)
            .withStyle("text-decoration: none; color: black; padding-right: 5px;");
    }

}
