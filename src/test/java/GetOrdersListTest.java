import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;

public class GetOrdersListTest {

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Basic test for GET /orders request")
    @Description("Check that GET /orders request returns list of orders (limit of 30 orders by default)")
    public void checkGETOrdersReturnsOrdersList() {
        // Создать тестовые заказы (на случай если тестовый стенд пустой)
        Order order1 = new Order("Марина", "Тестер", "ул.Ленина, д.1, кв.1", "215", "89111234567", 2, "2023-02-01", "", null);
        Order order2 = new Order("Иван", "Иванов", "Маяковская 1-111", "73", "+79057654322", 6, "2023-02-15", "домофон 111", null);
        createTestOrder(order1);
        createTestOrder(order2);
        // Вызов GET /orders
        Orders orders = given()
                        .get("/api/v1/orders")
                        .body().as(Orders.class);
        // Проверить количество заказов в списке
        Order[] list = orders.getOrders();
        Assert.assertTrue(list.length>1 && list.length<=30);
    }

    @Step
    @DisplayName("Send POST /orders request")
    @Description("Send POST /orders request to create test data")
    public void createTestOrder(Order order) {
    given()
                .header("Content-type", "application/json")
                .and()
                .body(order)
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(201);
}
}