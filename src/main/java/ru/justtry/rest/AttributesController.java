package ru.justtry.rest;

import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.justtry.database.Database;

import javax.inject.Inject;
import java.util.Map;

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
    public void save(@RequestBody Map<String, String> settings)
    {

    }

    @GetMapping("/get")
    @ResponseBody
    public String get()
    {
        return attr;
    }
}
