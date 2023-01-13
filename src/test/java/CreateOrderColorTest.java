import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class CreateOrderColorTest {

    private String firstName;
    private String lastName;
    private String address;
    private String metroStation;
    private String phone;
    private int rentTime;
    private String deliveryDate;
    private String comment;
    private String colorSet;

    public CreateOrderColorTest(String firstName, String lastName, String address, String metroStation, String phone, int rentTime, String deliveryDate, String comment, String colorSet) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.metroStation = metroStation;
        this.phone = phone;
        this.rentTime = rentTime;
        this.deliveryDate = deliveryDate;
        this.comment = comment;
        this.colorSet = colorSet;
    }

    @Parameterized.Parameters
    public static Object[][] getTestData() {
        return new Object[][]{
                {"Пётр", "Петров", "Москва, ул.Ленина 1-111", "215", "+79057654321", 3, "2023-01-25", "Позвоните за 2 часа", "BLACK, GREY"},
                {"Иван", "Иванов", "Арбат, 1, 111", "26", "89051234567", 1, "2023-02-11", "", ""},
                {"Мария", "Сидорова", "Лермонтовский проспект, д.7,стр.7-А,этаж 2, кв.77", "4", "89111234567", 2, "2023-02-01", "", "GREY"},
                {"Ян", "Ли", "ФИНЭК", "223", "89051234567", 1, "2023-01-17", "-", "BLACK"},
        };
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Test for POST /orders request with different scooter color preferences")
    @Description("Check that it is possible to create order with different scooter color preferences using API request")
    public void checkOrderCreationColorSelection() {
        String[] color = colorSet.split(", ");
        Order order = new Order(firstName, lastName, address, metroStation, phone, rentTime, deliveryDate, comment, color);
        Response response1 = sendPOSTOrdersRequest(order);
        int trackNumber = checkPOSTOrdersResponse(response1);
        OrderResponse response2 = sendGETOrdersRequest(trackNumber);
        Assert.assertEquals(color, response2.getColorFromOrder());
    }

    @Step("Send POST /orders request")
    public Response sendPOSTOrdersRequest(Order order) {
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(order)
                        .when()
                        .post("/api/v1/orders");
        return response;
    }

    @Step("Check POST /orders response is '201' and contains track number")
    public int checkPOSTOrdersResponse(Response response) {
        response.then().assertThat().body("track", notNullValue())
                .and()
                .statusCode(201);

        int track = response.then()
                .extract()
                .path("track");
        return track;
    }

    @Step("Send GET /orders request to confirm that order exists")
    public OrderResponse sendGETOrdersRequest (int track) {
        OrderResponse response =
                given()
                        .queryParam("t", track)
                        .get("/api/v1/orders/track")
                        .body().as(OrderResponse.class);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String result = gson.toJson(response);
        System.out.println("Трек: " + track + "\n" + result);
        return response;
    }
}
