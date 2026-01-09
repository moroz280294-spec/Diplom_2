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
import org.junit.experimental.categories.Categories;
import ru.yandex.practicum.models.User;
import ru.yandex.practicum.steps.UserSteps;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class CreateUserTest extends BaseTest {
    private User user;
    private final UserSteps userSteps = new UserSteps();

    @Before
    public void setUp() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        String randomPart = RandomStringUtils.randomAlphabetic(6);
        user = new User()
                .withEmail("User" + randomPart + "@example.com")
                .withPassword(RandomStringUtils.randomAlphabetic(8))
                .withFirstName("User" + randomPart);
    }

    @Test
    @DisplayName("Тест. Успешное создание пользователя")
    public void shouldCreateUserTest() {
        Response response = userSteps.createUser(user)
                .statusCode(SC_OK)
                .body("success", is(true))
                .extract().response();

        // Проверяем структуру user
        assertThat(response.path("user.email"), is(user.getEmail()));
        assertThat(response.path("user.name"), is(user.getFirstName()));

        // Проверяем наличие токенов,их точное значение не проверяем, только наличие и формат
        String accessToken = response.path("accessToken");
        String refreshToken = response.path("refreshToken");

        assertThat(accessToken, notNullValue());
        assertThat(refreshToken, notNullValue());
        assertThat(accessToken, startsWith("Bearer "));
    }

    @Test
    @DisplayName("Тест.Нельзя создать двух пользователей с одинаковым email")
    public void shouldNotAllowDuplicateUserEmailTest() {
        userSteps.createUser(user).statusCode(SC_OK);
        User duplicate = new User()
                .withEmail(user.getEmail())        //  та же почта
                .withPassword("different_password")   //  другой пароль
                .withFirstName("Different Name");     //  другое имя
        Response response = userSteps.createUser(duplicate).extract().response();

        assertThat(response.statusCode(), is(SC_FORBIDDEN));
        assertThat(response.path("success"), is(false));
        assertThat(response.path("message"), equalTo("User already exists"));
    }

    @Test
    @DisplayName("Тест. Нельзя создать пользователя без email")
    public void shouldNotCreateUserWithoutEmailTest() {
        User userWithoutEmail = new User()
                .withPassword(RandomStringUtils.randomAlphabetic(8))
                .withFirstName("UserWithoutEmail");

        userSteps.createUser(userWithoutEmail)
                .statusCode(SC_FORBIDDEN)
                .body("success", is(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Тест. Нельзя создать пользователя без пароля")
    public void shouldNotCreateUserWithoutPasswordTest() {
        String randomPart = RandomStringUtils.randomAlphabetic(6);
        User userWithoutPassword = new User()
                .withEmail("UserNoPass" + randomPart + "@example.com")
                .withFirstName("UserWithoutPassword");

        userSteps.createUser(userWithoutPassword)
                .statusCode(SC_FORBIDDEN)
                .body("success", is(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Тест. Нельзя создать пользователя без имени")
    public void shouldNotCreateUserWithoutNameTest() {
        String randomPart = RandomStringUtils.randomAlphabetic(6);
        User userWithoutName = new User()
                .withEmail("UserNoName" + randomPart + "@example.com")
                .withPassword(RandomStringUtils.randomAlphabetic(8));

        userSteps.createUser(userWithoutName)
                .statusCode(SC_FORBIDDEN)
                .body("success", is(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }
    @After
    public void tearDown() {
        if (user != null && user.getToken() != null) {
            try {
                userSteps.deleteUser(user).extract().response();
            } catch (Exception ignored) {}
        }
    }
}