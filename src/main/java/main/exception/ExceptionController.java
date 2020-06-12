package main.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class ExceptionController extends ResponseEntityExceptionHandler {


    @ExceptionHandler(value = { NullPointerException.class })
    protected ResponseEntity<Object> handleUncaughtException(
            NullPointerException ex
    ) {
        log.warn(ex.getMessage());
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    private ResponseEntity<Object> handle400(IllegalArgumentException e){
        log.warn(e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

}
