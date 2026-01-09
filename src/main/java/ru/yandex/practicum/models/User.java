package ru.yandex.practicum.models;

public class User {


    public User withToken(String token) {
        this.token = token;
        return  this;
    }

    public String getPassword() {
        return password;
    }

    public User withPassword(String password) {
        this.password = password;
        return  this;
    }

    public String getEmail() {
        return email;
    }

    public User withEmail(String email) {
        this.email = email;
        return  this;
    }

    public String getFirstName() {
        return firstName;
    }

    public User withFirstName(String firstName) {
        this.firstName = firstName;
        return  this;
    }

    public String email;
    private String password;
    private String firstName;

    public String getToken() {
        return token;
    }

    public String token;
}
