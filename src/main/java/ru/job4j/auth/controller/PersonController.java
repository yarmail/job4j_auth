package ru.job4j.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.auth.model.Person;
import ru.job4j.auth.service.PersonService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/persons")
public class PersonController {
    private  final PersonService personService;
    private BCryptPasswordEncoder encoder;

    public PersonController(PersonService personService,
                            BCryptPasswordEncoder encoder) {
        this.personService = personService;
        this.encoder = encoder;
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

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Person person) {
        Optional<Person> optionalPerson = personService.save(person);
        if (optionalPerson.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No person found for update");
        }
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
    public ResponseEntity<Void> signUp(@RequestBody Person person) {
        person.setPassword(encoder.encode(person.getPassword()));
        personService.save(person);
        return ResponseEntity.ok().build();
    }
}