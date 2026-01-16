package ru.yandex.practicum.steps;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import ru.yandex.practicum.models.User;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static config.RestConfig.CREATE_ORDER;
import static config.RestConfig.GET_INGREDIENTS;
import static io.restassured.RestAssured.given;

public class OrderSteps {

    @Step("Получение списка ингредиентов")
    public ValidatableResponse getIngredients() {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get(GET_INGREDIENTS)
                .then();
    }

    @Step("Выбор ингредиентов: булка, начинка, соус")
    public List<String> selectIngredientsForOrder(Response ingredientsResponse) {
        List<Map<String, Object>> ingredients = ingredientsResponse.jsonPath().getList("data");

        Map<String, String> byType = ingredients.stream()
                .collect(Collectors.toMap(
                        ing -> (String) ing.get("type"),
                        ing -> (String) ing.get("_id"),
                        (existing, replacement) -> existing // оставить первый
                ));

        return Arrays.asList(
                byType.get("bun"),
                byType.get("main"),
                byType.get("sauce")
        );
    }

    @Step("Создание заказа с авторизацией ")
    public ValidatableResponse createOrderWithAuth(User user, List<String> ingredients) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("ingredients", ingredients);
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", user.getAccessToken())
                .body(requestBody)
                .when()
                .post(CREATE_ORDER)
                .then();
    }

    @Step("Создание заказа без авторизации ")
    public ValidatableResponse createOrderWithoutAuth(List<String> ingredients) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of("ingredients", ingredients))
                .when()
                .post(CREATE_ORDER)
                .then();
    }
}
