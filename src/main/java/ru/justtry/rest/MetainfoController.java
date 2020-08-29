package ru.justtry.rest;

import java.util.ArrayList;

import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import ru.justtry.database.Database;
import ru.justtry.shared.Identifiable;

@CrossOrigin(maxAge = 3600)
public abstract class MetaInfoController implements Controller
{
    abstract String getCollectionName();

    @Inject
    protected Database database;


    @GetMapping(produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Identifiable[] get()
    {
        return getMapper().getObjects(database.getDocuments(getCollectionName(), new ArrayList<>()));
    }
}
