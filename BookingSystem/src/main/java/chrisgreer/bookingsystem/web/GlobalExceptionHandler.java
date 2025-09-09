package chrisgreer.bookingsystem.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    // Want to return Json of errors
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException exception
    ){

        var errors = new HashMap<String, String>();

        exception.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage())
        );

        exception.getBindingResult().getGlobalErrors().forEach(err ->
                errors.put(err.getObjectName(), err.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleJsonParseErrors(
            org.springframework.http.converter.HttpMessageNotReadableException ex) {

        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", "Invalid or missing JSON request body");
        return ResponseEntity.badRequest().body(body);
    }

}
