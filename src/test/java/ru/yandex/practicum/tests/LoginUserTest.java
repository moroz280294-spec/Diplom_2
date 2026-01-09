package ru.yandex.practicum.tests;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.practicum.models.User;
import ru.yandex.practicum.steps.UserSteps;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.*;

public class LoginUserTest extends BaseTest {
    private User user;
    private final UserSteps userSteps = new UserSteps();

    @Before
    public void setUp() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        String randomPart = RandomStringUtils.randomAlphabetic(6).toLowerCase();
        user = new User()
                .withEmail("user" + randomPart + "@example.com")
                .withPassword(RandomStringUtils.randomAlphabetic(8))
                .withFirstName("user" + randomPart);
        Response createResponse = userSteps.createUser(user).extract().response();
        user.withAccessToken(createResponse.path("accessToken"));
    }

    @Test
    @DisplayName("Тест. Успешный логин пользователя")
    public void shouldLoginUserTest() {
        userSteps.loginUser(user)
                .statusCode(SC_OK)
                .body("success", is(true))
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name", equalTo(user.getFirstName()))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("accessToken", startsWith("Bearer "));
    }

    @Test
    @DisplayName("Тест. Нельзя залогиниться с несуществующим email")
    public void shouldNotLoginWithNonExistentEmailTest() {
        String randomPart = RandomStringUtils.randomAlphabetic(6);
        User userWithNonExistentEmail = new User()
                .withEmail("NonExistent" + randomPart + "@example.com")
                .withPassword(RandomStringUtils.randomAlphabetic(8));

        userSteps.loginUser(userWithNonExistentEmail)
                .statusCode(SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Тест. Нельзя залогиниться с некорректным паролем")
    public void shouldNotLoginWithIncorrectPasswordTest() {
        // Пытаемся залогиниться с неправильным паролем
        User userWithWrongPassword = new User()
                .withEmail(user.getEmail())
                .withPassword("wrong_password_12345");

        userSteps.loginUser(userWithWrongPassword)
                .statusCode(SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", equalTo("email or password are incorrect"));
    }


    @After
    public void tearDown() {
        if (user != null && user.getAccessToken() != null) {
            try {
                userSteps.deleteUser(user).extract().response();
            } catch (Exception ignored) {
            }
        }
    }
}
