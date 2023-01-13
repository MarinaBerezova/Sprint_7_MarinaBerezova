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
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class CreateOrderTest {

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("POST /orders: check that 'color' field is optional")
    @Description("Check that POST /orders request can be sent without 'color' field in request body")
    public void checkOrderCreationNoColor() {
        Order order = new Order("Marina", "Tester", "1-11, Test street, Test city", "215", "89111234567", 5, "2023-02-01", "2nd floor", null);
        Response response1 = sendPOSTOrdersRequest(order);
        int trackNumber = checkPOSTOrdersResponse(response1);
        OrderResponse response2 = sendGETOrdersRequest(trackNumber);
        Assert.assertEquals(null, response2.getColorFromOrder());
    }

    @Test
    @DisplayName("POST /orders: response body contains 'track'")
    @Description("Check that POST /orders response body contains 'track'")
    public void checkPOSTOrdersReturnsTrack() {
        Order order = new Order("Marina", "Tester", "1-11, Test street, Test city", "215", "89111234567", 5, "2023-02-01", "2nd floor", null);
        Response response = sendPOSTOrdersRequest(order);
        int track = checkPOSTOrdersResponse(response);
        Assert.assertTrue(track > 0);
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