package ru.justtry.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.justtry.attributes.Entity;
import ru.justtry.database.Database;

import javax.inject.Inject;

import java.util.Arrays;

import static ru.justtry.attributes.EntityConstants.ATTRIBUTES;
import static ru.justtry.shared.Constants.*;

@RestController
@RequestMapping("/rest/entities")
public class EntityController
{
    @Inject
    private Database database;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void save(
            @RequestParam(value = NAME) String name,
            @RequestParam(value = ATTRIBUTES) String attributes[])
    {
        Entity entity = new Entity();

        entity.setName(name);
        entity.setAttributes(Arrays.asList(attributes));

        database.saveEntity(entity);
    }

    @GetMapping("/{name}")
    @ResponseBody
    public Entity get(@PathVariable(value = NAME) String name)
    {
        return database.getEntity(name);
    }

    @GetMapping
    @ResponseBody
    public Object[] getAll()
    {
        return database.getEntities();
    }
}
