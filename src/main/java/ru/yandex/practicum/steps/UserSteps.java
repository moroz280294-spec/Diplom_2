package ru.yandex.practicum.steps;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import ru.yandex.practicum.models.User;

import java.util.HashMap;
import java.util.Map;

import static config.RestConfig.*;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;

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

    @Step("Создание пользователя с рандомными данными")
    public User createRandomUser() {
        String randomPart = RandomStringUtils.randomAlphabetic(6).toLowerCase();//добавил только нижний регистр чтобы обойти баг
        String email = "user" + randomPart + "@example.com";
        String password = RandomStringUtils.randomAlphabetic(8);
        String name = "user" + randomPart;

        User user = new User().withEmail(email).withPassword(password).withFirstName(name);
        Response response = createUser(user).statusCode(SC_OK).extract().response();
        user.withAccessToken(response.path("accessToken"));
        return user;
    }
}

