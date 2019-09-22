package ru.justtry.rest;

import com.google.common.base.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.justtry.attributes.Attribute;
import ru.justtry.database.Database;

import javax.inject.Inject;

import static ru.justtry.attributes.AttributeConstants.*;
import static ru.justtry.shared.Constants.*;

// { "Name": "State", "Method": "none",  "Visible": true, "Type": "text", "Min-width": 100, "Max-width": 100, "Align": "center" }

@RestController
@RequestMapping("/rest/attributes")
public class AttributesController
{
    @Inject
    private Database database;

    private String attr = "{ \"Name\": \"State\", \"Method\": \"none\",  \"Visible\": true," +
            " \"Type\": \"text\", \"Min-width\": 100, \"Max-width\": 100, \"Align\": \"center\" }";

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void save(
            @RequestParam(value = NAME, required = true) String name,
            @RequestParam(value = METHOD, required = false) String method,
            @RequestParam(value = VISIBLE, required = true) Boolean visible,
            @RequestParam(value = TYPE, required = true) String type,
            @RequestParam(value = MIN_WIDTH, required = false) String minWidth,
            @RequestParam(value = MAX_WIDTH, required = false) String maxWidth,
            @RequestParam(value = LINES_COUNT, required = false) Integer linesCount,
            @RequestParam(value = ALIGNMENT, required = false) String alignment)
    {

        // TODO remake for mapping
        Attribute attribute = new Attribute();
        attribute.setName(name);
        attribute.setMethod(Strings.isNullOrEmpty(method) ? NONE : method);
        attribute.setVisible(visible);
        attribute.setType(type);
        attribute.setMinWidth(Strings.isNullOrEmpty(minWidth) ? DEFAULT_WIDTH : minWidth);
        attribute.setMaxWidth(Strings.isNullOrEmpty(maxWidth) ? DEFAULT_WIDTH : maxWidth);
        attribute.setLinesCount(linesCount == null ? 1 : linesCount);
        attribute.setAlignment(Strings.isNullOrEmpty(alignment) ? LEFT : alignment);

        database.saveAttribute(attribute);
    }

    @GetMapping
    @ResponseBody
    public Object[] getAll()
    {
        return database.getAttributes();
    }

    @GetMapping("/{name}")
    @ResponseBody
    public Attribute get(@PathVariable(value = NAME) String name)
    {
        return database.getAttribute(name);
        //database.createTestData();
        //return attr;
    }
}
