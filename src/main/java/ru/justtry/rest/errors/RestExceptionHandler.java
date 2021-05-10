package ru.justtry.rest.errors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.justtry.shared.Utils;


@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class RestExceptionHandler extends ResponseEntityExceptionHandler
{
    private final Utils utils;


    @ExceptionHandler(Exception.class)
    @ResponseBody
    public final ResponseEntity<Object> handleAllExceptions(Exception e, WebRequest request)
    {
        log.error(e.toString());
        return utils.getResponseForError(new HttpHeaders(), e);
    }
}
