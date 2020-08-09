package ru.justtry.rest;

import ru.justtry.mappers.Mapper;
import ru.justtry.validation.Validator;

public interface Controller
{
    Mapper getMapper();
    Validator getValidator();
}
