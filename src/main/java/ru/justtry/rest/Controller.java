package ru.justtry.rest;

import ru.justtry.mappers.Mapper;
import ru.justtry.postprocessing.Postprocessor;
import ru.justtry.validation.Validator;

public interface Controller
{
    Mapper getMapper();
    Validator getValidator();
    Postprocessor getSavePostprocessor();
    Postprocessor getDeletePostprocessor();
}
