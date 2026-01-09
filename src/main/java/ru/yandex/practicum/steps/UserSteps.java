package ru.yandex.practicum.steps;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import ru.yandex.practicum.models.User;

import java.util.HashMap;
import java.util.Map;

import static config.RestConfig.*;
import static io.restassured.RestAssured.given;

public class UserSteps {

    @Step("Создание пользователя")
    public ValidatableResponse createUser(User user) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", user.getEmail());
        requestBody.put("password", user.getPassword());
        requestBody.put("name", user.getFirstName());

        return given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(REGISTER_USER)
                .then();
    }

    @Step("Логин пользователя")
    public ValidatableResponse loginUser(User user) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", user.getEmail());
        requestBody.put("password", user.getPassword());

        return given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(USER_LOGIN)
                .then();
    }

    @Step("Удаление пользователя")
    public ValidatableResponse deleteUser(User user) {
        return given()
                .header("Authorization", user.getAccessToken())
                .when()
                .delete(DELETE_USER)
                .then();
    }
}

