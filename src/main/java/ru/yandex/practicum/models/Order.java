package ru.yandex.practicum.models;

import java.util.List;

public class Order {
    // Входные данные (для создания заказа)
    private List<String> ingredients;

    // Выходные данные (из ответа API)
    private String name;
    private long orderNumber;

    // Конструктор по умолчанию (обязателен для RestAssured/Jackson)
    public Order() {
    }

    // Конструктор для удобства
    public Order(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    // Геттеры и сеттеры
    public List<String> getIngredients() {
        return ingredients;
    }

    public Order setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
        return this;
    }

    public String getName() {
        return name;
    }

    public Order setName(String name) {
        this.name = name;
        return this;
    }

    public long getOrderNumber() {
        return orderNumber;
    }

    public Order setOrderNumber(long orderNumber) {
        this.orderNumber = orderNumber;
        return this;
    }
}
