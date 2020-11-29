package ru.justtry.rest.errors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import ru.justtry.shared.Utils;


@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler
{
    final static Logger logger = LogManager.getLogger(RestExceptionHandler.class);

    @Autowired
    private Utils utils;


    @ExceptionHandler(Exception.class)
    @ResponseBody
    public final ResponseEntity<Object> handleAllExceptions(Exception e, WebRequest request)
    {
        logger.error(e);
        return utils.getResponseForError(new HttpHeaders(), e);
    }
}
