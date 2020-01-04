package ru.justtry.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.justtry.metainfo.Attribute;
import ru.justtry.mappers.AttributeMapper;
import ru.justtry.validation.AttributeValidator;
import ru.justtry.mappers.Mapper;
import ru.justtry.validation.Validator;

import javax.inject.Inject;

import static ru.justtry.shared.AttributeConstants.*;

// { "Name": "State", "Method": "none",  "Visible": true, "Type": "text", "Min-width": 100, "Max-width": 100, "Align": "center" }

@RestController
@RequestMapping("/rest/attributes")
public class AttributesController extends MetainfoController
{
    @Inject
    private AttributeValidator attributeValidator;
    @Inject
    private AttributeMapper attributeMapper;


    //@PostMapping(consumes = "application/json", produces = "application/json")
    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public String save(@RequestBody Attribute attribute)
//            @RequestParam(value = NAME) String name,
//            @RequestParam(value = TYPE) String type,
//            @RequestParam(value = VISIBLE) Boolean visible,
//            @RequestParam(value = METHOD, required = false) String method,
//            @RequestParam(value = MIN_WIDTH, required = false) String minWidth,
//            @RequestParam(value = MAX_WIDTH, required = false) String maxWidth,
//            @RequestParam(value = MIN_VALUE, required = false) String minValue,
//            @RequestParam(value = MAX_VALUE, required = false) String maxValue,
//            @RequestParam(value = DEFAULT, required = false) String defaultValue,
//            @RequestParam(value = LINES_COUNT, required = false) Integer linesCount,
//            @RequestParam(value = ALIGNMENT, required = false) String alignment)
    {
//        Attribute attribute = new Attribute();
//        attribute.setName(name);
//        attribute.setMethod(Strings.isNullOrEmpty(method) ? NONE : method);
//        attribute.setVisible(visible);
//        attribute.setType(type);
//        attribute.setMinWidth(Strings.isNullOrEmpty(minWidth) ? DEFAULT_WIDTH : minWidth);
//        attribute.setMaxWidth(Strings.isNullOrEmpty(maxWidth) ? DEFAULT_WIDTH : maxWidth);
//        attribute.setMinValue(minValue);
//        attribute.setMaxValue(maxValue);
//        attribute.setDefaultValue(defaultValue);
//        attribute.setLinesCount(linesCount == null ? 1 : linesCount);
//        attribute.setAlignment(Strings.isNullOrEmpty(alignment) ? LEFT : alignment);

        return database.saveDocument(ATTRIBUTES_COLLECTION, getValidator(), getMapper(), attribute);
       // return database.saveAttribute(attribute);
    }

    @PutMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public void update(@RequestBody Attribute attribute)
    {
        database.updateDocument(ATTRIBUTES_COLLECTION, getValidator(), getMapper(), attribute);
    }


//    @PostMapping
//    @ResponseStatus(HttpStatus.OK)
//    public void update()
//    {
//
//    }


//    @GetMapping("/{name}")
//    @ResponseBody
//    public Attribute get(@PathVariable(value = NAME) String name)
//    {
//        return database.getAttribute(name);
//        //database.createTestData();
//        //return attr;
//    }


    @Override
    protected Mapper getMapper()
    {
        return attributeMapper;
    }

    @Override
    protected Validator getValidator()
    {
        return attributeValidator;
    }

    @Override
    protected String getCollectionName()
    {
        return ATTRIBUTES_COLLECTION;
    }
}
