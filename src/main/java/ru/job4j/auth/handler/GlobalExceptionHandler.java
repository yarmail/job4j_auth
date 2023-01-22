package ru.job4j.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ControllerAdvise используемая совместно с @ExceptionHandler.
 * Код ниже обрабатывает все исключения NullPointerException,
 * которые возникают во всех контроллерах
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(value = {NullPointerException.class})
    public void nullPointer(Exception e, HttpServletRequest request,
                                    HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new HashMap<>() {{
            put("message", "Some of fields empty");
            put("details", e.getMessage());
        }}));
    }

    /**
     * Следующий момент, который стоит отметить, это то,
     * что если валидация не проходит, то в контексте приложения
     * кидается исключение MethodArgumentNotValidException,
     * поэтому для того чтобы возвращать понятные ответы клиенту
     * нужно его отлавливать. С тем как это сделать мы уже знакомы.
     * Воспользуемся одним из способов - @ControllerAdivise + @ExceptionHandler.
     *
     * Важно! MethodArgumentNotValidException возникает только
     * если @Valid используется для аргумента метода.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> notValid(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(
                e.getFieldErrors().stream()
                        .map(f -> Map.of(
                                f.getField(),
                                String.format("%s. Actual value: %s", f.getDefaultMessage(),
                                        f.getRejectedValue())))
                        .collect(Collectors.toList())
        );
    }
}