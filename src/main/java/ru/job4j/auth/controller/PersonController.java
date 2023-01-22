package ru.job4j.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.auth.dto.PersonDTO;
import ru.job4j.auth.model.Person;
import ru.job4j.auth.service.PersonService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/persons")
public class PersonController {
    private  final PersonService personService;
    private BCryptPasswordEncoder encoder;
    private final ObjectMapper objectMapper;

    public PersonController(PersonService personService,
                            BCryptPasswordEncoder encoder, ObjectMapper objectMapper) {
        this.personService = personService;
        this.encoder = encoder;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/all")
    public List<Person> findAll() {
        return personService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> findById(@PathVariable int id) {
        var person = this.personService.findById(id);
        return new ResponseEntity<Person>(
                person.orElse(new Person()),
                person.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    /**
     * ResponseStatusException
     * Если не нужно как-то отслеживать исключения приложения,
     * прописывать сложную логику, указывать специфические детали об ошибках и т.п.,
     * можно просто пробрасывать исключение ResponseStatusException.
     */
    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Person person) {
        Optional<Person> optionalPerson = personService.save(person);
        if (optionalPerson.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No person found for update");
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Пример использования DTO для форм где нужны просто логин и пароль
     */
    @PatchMapping("/")
    public ResponseEntity<String> updatePassword(@RequestBody @Valid
                                                 PersonDTO personDTO) {
        Optional<Person> optionalPerson = personService.findByLogin(personDTO.getLogin());
        if (optionalPerson.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No person found for update");
        }
        Person person = optionalPerson.get();
        person.setPassword(personDTO.getPassword());
        personService.save(person);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        if (!personService.deleteById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No persons with this id");
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Пароли хешируются и прямом виде не хранятся в базе.
     */
    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(@RequestBody @Valid
                                       Person person) {
        var username = person.getLogin();
        var password = person.getPassword();
        if (username == null || password == null) {
            throw new NullPointerException("Username and password mustn't be empty");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Invalid password");
        }
        person.setPassword(encoder.encode(person.getPassword()));
        personService.save(person);
        return ResponseEntity.ok().build();
    }

    /**
     * @ExceptionHandler - Данная аннотация позволяет отслеживать
     * и обрабатывать исключения на уровне класса. Если использовать
     * ее например в контроллере, то исключения только данного
     * контроллера будут обрабатываться.
     */
    @ExceptionHandler(value = {IllegalArgumentException.class})
    public void handlerException(Exception e, HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new HashMap<>() {{
            put("message", "Some of fields empty");
            put("details", e.getMessage());
        }}));
    }
}