package ru.job4j.auth.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Data
@Table(name = "person")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "login")
    @NotBlank(message = "Login must be not empty")
    @Size(min = 3, max = 15, message = "Login should be between 3 and 15 characters")
    private String login;

    @Column(name = "password")
    @NotBlank(message = "Password must be not empty")
    @Size(min = 6, message = "must be more than 6 characters")
    private String password;
}