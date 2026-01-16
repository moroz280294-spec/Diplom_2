package ru.yandex.practicum.tests;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
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


        user = userSteps.createRandomUser();
    }

    @Test
    @DisplayName("Тест. Успешный логин пользователя")
    @Description("Проверка успешного входа пользователя с валидными учетными данными. Проверяется корректность ответа, наличие токенов доступа и обновления.")
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
    @Description("Проверка невозможности входа с несуществующим email. Система должна вернуть ошибку 'email or password are incorrect'.")
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
    @Description("Проверка невозможности входа с некорректным паролем для существующего пользователя. Система должна вернуть ошибку 'email or password are incorrect'.")
    public void shouldNotLoginWithIncorrectPasswordTest() {

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