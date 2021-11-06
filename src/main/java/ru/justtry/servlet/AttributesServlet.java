package ru.justtry.servlet;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.input;
import static j2html.TagCreator.span;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import j2html.tags.DomContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.Entity;
import ru.justtry.rest.controllers.AttributesController;
import ru.justtry.rest.controllers.EntitiesController;
import ru.justtry.shared.ResourceUtils;

/**
 * Servlet to display information about existing attributes.
 */
@RequiredArgsConstructor
@WebServlet(name = "attributes", description = "Attributes information", urlPatterns = "/attributes")
@Slf4j
public class AttributesServlet extends HttpServlet
{
    private final AttributesController attributesController;
    private final EntitiesController entitiesController;


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.setContentType("text/html");
        ServletOutputStream outputStream = response.getOutputStream();

        try
        {
            String htmlTemplate = ResourceUtils.readTextFile("web/template.html");
            String content = createContent().render();
            outputStream.write(String.format(htmlTemplate, new Date().toString(), content)
                .getBytes(StandardCharsets.UTF_8));
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
            input().withType("button").withValue("New attribute")
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
            span("Type").withStyle("font-weight: bold;"),
            span("Entities").withStyle("font-weight: bold;"),
            span(),
            span()
        );
    }

    private DomContent createTableContent()
    {
        Attribute[] attributes = attributesController.get();
        Entity[] entities = entitiesController.get();
        List<AttributeDto> attributeDtos = new ArrayList<>();
        for (Attribute attribute : attributes)
        {
            List<String> entityNames = Arrays.stream(entities)
                .filter(x -> x.getAttributes().contains(attribute.getName()))
                .map(Entity::getName)
                .collect(Collectors.toList());
            attributeDtos.add(new AttributeDto(attribute, entityNames));
        }

        return each(attributeDtos, this::createTableRow);
    }

    private DomContent createTableRow(Integer index, AttributeDto attributeDto)
    {
        Attribute attribute = attributeDto.getAttribute();

        return each(
            span(String.valueOf(index + 1)).withStyle("justify-self: right; text-align: right;"),
            span(attribute.getTitle()),
            span(attribute.getName()),
            span(attribute.getType()),
            createEntityLinks(attributeDto),
            span().withClass("editButton").attr("attribute-name", attribute.getName()),
            span().withClass("deleteButton").attr("attribute-name", attribute.getName())
        );
    }

    private DomContent createEntityLinks(AttributeDto attributeDto)
    {
        return span(
            each(attributeDto.entityNames, this::createEntityLink)
        ).withClass("usage-list").withStyle("display: flex;");
    }

    private DomContent createEntityLink(String entityName)
    {
        return a(entityName)
            .withHref("/entities/entity/" + entityName)
            .withStyle("text-decoration: none; color: black; padding-right: 5px;");
    }


    @Data
    @AllArgsConstructor
    private static class AttributeDto
    {
        private Attribute attribute;
        private List<String> entityNames;
    }

}
