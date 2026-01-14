package ru.yandex.practicum.tests;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.practicum.models.User;
import ru.yandex.practicum.steps.OrderSteps;
import ru.yandex.practicum.steps.UserSteps;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.*;

public class CreateOrderTest extends BaseTest {
    private User user;
    private final UserSteps userSteps = new UserSteps();
    private final OrderSteps orderSteps = new OrderSteps();

    @Before
    public void setUp() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @Test
    @DisplayName("Тест. Успешное создание заказа с авторизацией, с валидными ингредиентами")
    @Description("Проверка успешного создания заказа авторизованным пользователем с валидными ингредиентами (булка, начинка, соус). Проверяется корректность ответа и наличие номера заказа.")
    public void shouldCreateOrderWithAuthTest() {
        // Создание и авторизация пользователя
        user = userSteps.createRandomUser();

        // Получение списка ингредиентов
        Response ingredientsResponse = orderSteps.getIngredients()
                .statusCode(SC_OK)
                .extract().response();

        List<String> orderIngredients = orderSteps.selectIngredientsForOrder(ingredientsResponse);

        // Создание заказа с авторизацией
        orderSteps.createOrderWithAuth(user, orderIngredients)
                .statusCode(SC_OK)
                .body("success", is(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа: без авторизации, с валидными ингредиентами")
    @Description("Проверка успешного создания заказа неавторизованным пользователем с валидными ингредиентами. Система должна разрешить создание заказа без авторизации.")
    public void shouldCreateOrderWithoutAuthWithValidIngredients() {
        Response ingredientsResponse = orderSteps.getIngredients()
                .statusCode(SC_OK)
                .extract().response();

        List<String> orderIngredients = orderSteps.selectIngredientsForOrder(ingredientsResponse);

        orderSteps.createOrderWithoutAuth(orderIngredients)
                .statusCode(SC_OK)
                .body("success", is(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа: с авторизацией, без ингредиентов")
    @Description("Проверка невозможности создания заказа авторизованным пользователем без указания ингредиентов. Система должна вернуть ошибку валидации.")
    public void shouldNotCreateOrderWithAuthAndNoIngredients() {
        // Создание и авторизация пользователя
        user = userSteps.createRandomUser();

        orderSteps.createOrderWithAuth(user, Collections.emptyList())
                .statusCode(SC_BAD_REQUEST)
                .body("success", is(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа: без авторизации, без ингредиентов")
    @Description("Проверка невозможности создания заказа неавторизованным пользователем без указания ингредиентов. Система должна вернуть ошибку валидации.")
    public void shouldNotCreateOrderWithoutAuthAndNoIngredients() {
        orderSteps.createOrderWithoutAuth(Collections.emptyList())
                .statusCode(SC_BAD_REQUEST)
                .body("success", is(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа: с авторизацией, с неверным хешем ингредиентов")
    @Description("Проверка обработки ошибки при создании заказа с неверными идентификаторами ингредиентов. Система должна вернуть ошибку сервера.")
    public void shouldNotCreateOrderWithInvalidIngredientHash() {
        // Создание и авторизация пользователя
        user = userSteps.createRandomUser();

        List<String> invalidIngredients = Arrays.asList("invalid_id_123", "not_a_real_id");

        orderSteps.createOrderWithAuth(user, invalidIngredients)
                .statusCode(SC_INTERNAL_SERVER_ERROR);
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

