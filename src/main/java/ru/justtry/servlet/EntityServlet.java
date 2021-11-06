package ru.justtry.servlet;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.input;
import static j2html.TagCreator.label;
import static j2html.TagCreator.span;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import j2html.tags.DomContent;
import j2html.tags.specialized.TableTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.justtry.metainfo.Attribute;
import ru.justtry.metainfo.Entity;
import ru.justtry.metainfo.dictionary.TableSide;
import ru.justtry.rest.controllers.AttributesController;
import ru.justtry.rest.controllers.EntitiesController;
import ru.justtry.shared.ResourceUtils;

/**
 * Servlet to display information about one single entity.
 */
@RequiredArgsConstructor
@WebServlet(name = "entity", description = "Entity information", urlPatterns = "/entities/entity/*")
@Slf4j
public class EntityServlet extends HttpServlet
{
    private static final String DISPLAY_NONE = "display: none; ";
    private static final String DISPLAY_TABLE_ROW = "display: table-row; ";


    private final EntitiesController entitiesController;
    private final AttributesController attributesController;


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.setContentType("text/html");
        ServletOutputStream outputStream = response.getOutputStream();
        String requestUri = request.getRequestURI();

        try
        {
            String htmlTemplate = ResourceUtils.readTextFile("web/template.html");
            String div = createContent(request.getRequestURI()).render();
            outputStream.write(String.format(htmlTemplate, new Date().toString(), div).getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e)
        {
            // TODO if there is an error put it into the error-label
            String message = Optional.ofNullable(e.getMessage()).orElse("");
            outputStream.write(message.getBytes(StandardCharsets.UTF_8));
            log.error("Error while handling GET request: {}", message, e);
        }
        finally
        {
            outputStream.close();
        }
    }

    private DomContent createContent(String uri)
    {
        Entity entity = getEntity(uri);

        return div(
            label("Title").withId("entity-title-label").withFor("entity-title"),
            input().withId("entity-title").withType("text").attr("attribute-name", "title"),
            label("Name (Unique)").withId("entity-name-label").withFor("entity-name"),
            input().withId("entity-name").withType("text").attr("attribute-name", "name"),
            label("Visible").withId("entity-visible-label").withFor("entity-visible"),
            input().withId("entity-visible").withClass("doNotStretch").withType("checkbox")
                .attr("attribute-name", "visible"),
            label("Attributes").withId("attributes-label"),
            div(),
            createAttributesTables(entity),
            createObjectButtons()


        ).withId("data-element").withStyle("display: grid;");
    }

    private DomContent createAttributesTables(Entity entity)
    {
        Attribute[] attributes = (Attribute[]) attributesController.get(null, null, null, true);
        return div(
            createAttributesTable(TableSide.LEFT, entity, attributes).withStyle("margin-right: 10px;"),
            createAttributesTable(TableSide.RIGHT, entity, attributes).attr("attribute-name", "attributes")
        ).withId("attributes-select").withClass("twoCols");
    }

    private TableTag createAttributesTable(TableSide side, Entity entity, Attribute[] attributes)
    {
        return table(
            thead(
                getAttributesTableHead(entity)
            ),
            tbody(
                each(Arrays.asList(attributes), x -> createAttributesTableRow(x, side, entity))
            )
        ).withClass("attributes-select-table");
    }

    private DomContent createAttributesTableRow(Attribute attribute, TableSide side, Entity entity)
    {
        boolean isUsed = entity.getAttributes().contains(attribute.getName());

        return tr(
            td(attribute.getName()).withStyle("cursor: pointer; "),
            td(attribute.getTitle()).withStyle("cursor: pointer; "),
            td(
                a()
                    .withId(String.format("%s-%s-button", attribute.getName(), side.getValue()))
                    .withClasses("image-icon", getClassName(side))
                    .withStyle(String.format("margin-left: 5px; margin-right: 5px; background-image: url(\"%s\"); ",
                        getImagePath(side)))
            ).withStyle("width: 0px; "),
            createIconButtons(side, entity, attribute, isUsed)
        ).withId(String.format("%s-%s", attribute.getName(), side.getValue()))
            .attr("attribute-name", attribute.getName())
            .attr("related-row", String.format("%s-%s", attribute.getName(), side.getOpposite().getValue()))
            .withStyle(getStyleDisplay(side, isUsed))
            .isDraggable();
    }

    private DomContent createTableHeader()
    {
        return each(
            span().withText("№").withStyle("justify-self: center; text-align: center; font-weight: bold;"),
            span().withText("Title").withStyle("font-weight: bold;"),
            span().withText("Name").withStyle("font-weight: bold;"),
            span().withText("Visible").withStyle("font-weight: bold;"),
            span().withText("Attributes").withStyle("font-weight: bold;"),
            span(),
            span()
        );
    }

    private DomContent createObjectButtons()
    {
        return div(
            input().withId("save-button").withClass("doNotStretch").withType("button").withValue("save")
                .withStyle("display: grid;"),
            input().withId("cancel-button").withClass("doNotStretch").withType("button").withValue("save")
                .withStyle("display: grid;")
        ).withClasses("objectButtons", "twoCols");
    }

    private String getClassName(TableSide side)
    {
        return (side == TableSide.LEFT) ? "plus-image" : "minus-image";
    }

    private String getImagePath(TableSide side)
    {
        return (side == TableSide.LEFT) ? "/img/plus.svg" : "/img/minus.svg";
    }

    private String getStyleDisplay(TableSide side, boolean isUsed)
    {
        switch (side)
        {
        case LEFT: return isUsed ? DISPLAY_NONE : DISPLAY_TABLE_ROW;
        case RIGHT: return isUsed ? DISPLAY_TABLE_ROW : DISPLAY_NONE;
        default: throw new IllegalArgumentException("Unknown side " + side);
        }
    }

    private Entity getEntity(String uri)
    {
        String entityName = uri.replace("/entities/entity", "");
        entityName = StringUtils.strip(entityName, "/");
        if (entityName.length() == 0)
            return null;

        return entitiesController.getByName(entityName);
    }

    private DomContent getAttributesTableHead(Entity entity)
    {
        if (entity != null)
            return tr(th("Name"), th("Title"), th());
        else
            return tr(th("Name"), th("Title"), th(), th(), th(), th(), th());
    }

    private DomContent createIconButtons(TableSide side, Entity entity, Attribute attribute, boolean isUsed)
    {
        if (side == TableSide.LEFT)
            return each();

        boolean isVisible = entity.getVisibleAttributes().contains(attribute.getName());
        boolean isCompared = entity.getComparedAttributes().contains(attribute.getName());
        boolean isKey = entity.getKeyAttribute().equals(attribute.getName());
        boolean isSort = entity.getSortAttribute().equals(attribute.getName());

        return each(
            // Key button
            td(
                a()
                    .withId(String.format("%s-key-button", attribute.getName()))
                    .withClasses("image-icon", isKey ? "selected-key-attribute-image" : "key-attribute-image")
                    .withStyle("margin-left: 5px; margin-right: 5px; " + getStyleKeyBackgroundImage(isKey)
            ).withStyle("text-align: center; width: 0px; "),
            // Sort button
            td(

            ).withStyle("text-align: center; width: 0px; "),
            // Visible in table button
            td(

            ).withStyle("text-align: center; width: 0px; "),
            // Visible in compared attributes button
            td(

            ).withStyle("text-align: center; width: 0px; ")
        ));

    }

    private String getStyleKeyBackgroundImage(boolean isKey)
    {
        String url = isKey ? "/img/selectedKeyAttribute.svg" : "/img/keyAttribute.svg";
        return String.format("background-image: url(\"%s\"); ", url);
    }

}
