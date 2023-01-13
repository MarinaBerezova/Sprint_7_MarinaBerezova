import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;

public class AddCourierTest {

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Basic test for POST /courier request")
    @Description("Check that Courier creation API request returns status code '201'")
    public void checkCourierCreationSuccess() {
        Courier courier = new Courier("auto0101", "qwerty1234", "Marina");
        Response response = sendPOSTRequestCourier(courier);
        checkPOSTResponseCourierIs201(response);
        clearTestData(courier);
    }

    @Test
    @DisplayName("POST /courier: response body contains 'ok: true'")
    @Description("Check that POST /courier response body contains 'ok: true'")
    public void checkPOSTCourierReturnsOkTrue() {
        Courier courier = new Courier("auto0102", "qwerty1234", "Marina");
        Response response = sendPOSTRequestCourier(courier);
        checkPOSTResponseBodyCourier(response);
        clearTestData(courier);
    }

    @Test
    @DisplayName("POST /courier: Duplicated couriers are not allowed")
    @Description("This test is to confirm that creation of duplicated couriers using API request is not allowed.")
    public void checkDuplicatedCouriersAreNotAllowed() {
        Courier courier = new Courier("auto0103", "qwerty1234", "Marina");
        Response response1 = sendPOSTRequestCourier(courier);
        checkPOSTResponseCourierIs201(response1);
        Response response2 = sendPOSTRequestCourier(courier);
        checkPOSTResponseCourierIs409(response2);
        clearTestData(courier);
    }

    @Test
    @DisplayName("POST /courier: Couriers with the same login are not allowed")
    @Description("This test is to confirm that couriers with the same login are not allowed.")
    public void checkDuplicatedCourierLoginsAreNotAllowed() {
        Courier courier1 = new Courier("auto0104", "qwerty1234", "Marina");
        Courier courier2 = new Courier("auto0104", "test321", "Elena");
        Response response1 = sendPOSTRequestCourier(courier1);
        checkPOSTResponseCourierIs201(response1);
        Response response2 = sendPOSTRequestCourier(courier2);
        checkPOSTResponseCourierIs409(response2);
        // Проверить, что первый курьер создан, а второй нет:
        checkPOSTRequestLoginReturns404(courier2);
        clearTestData(courier1);
    }

    @Step("Send POST /courier request")
    public Response sendPOSTRequestCourier(Courier courier) {
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(courier)
                        .when()
                        .post("/api/v1/courier");
        return response;
    }

    @Step("Check POST /courier response is '409 Conflict'")
    public void checkPOSTResponseCourierIs409(Response response) {
        response.then().assertThat().body("message", equalTo("Этот логин уже используется. Попробуйте другой."))
                .and()
                .statusCode(409);
    }

    @Step("Check POST /courier returns code '201'")
    public void checkPOSTResponseCourierIs201(Response response) {
        response.then().statusCode(201);
    }

    @Step("Check POST /courier response body contains 'ok: true'")
    public void checkPOSTResponseBodyCourier(Response response) {
        response.then().assertThat().body("ok", equalTo(true));
    }

    @Step("Send POST /courier/login, check response is '200' and get Courier ID")
    public int sendPOSTRequestLoginAndGetCourierID(Courier courier) {
        CourierCredentials credentials = new CourierCredentials(courier.getLogin(), courier.getPassword());
        int courierId =
                given()
                        .header("Content-Type", "application/json")
                        .body(credentials)
                        .when()
                        .post("/api/v1/courier/login")
                        .then()
                        .statusCode(200)
                        .extract()
                        .path("id");
        return courierId;
    }

    @Step("Send POST /courier/login for non-existing courier, check response is '404'")
    public void checkPOSTRequestLoginReturns404(Courier courier) {
        CourierCredentials credentials = new CourierCredentials(courier.getLogin(), courier.getPassword());
                given()
                        .header("Content-Type", "application/json")
                        .body(credentials)
                        .when()
                        .post("/api/v1/courier/login")
                        .then().assertThat().body("message", equalTo("Учетная запись не найдена"))
                        .and()
                        .statusCode(404);
    }


    @Step("Send DELETE /courier/{id} to remove test data")
    public void sendDELETERequestCourierByID(int courierId) {
                given()
                        .delete("/api/v1/courier/{courierId}", courierId)
                        .then()
                        .statusCode(200);

        //Повторный вызов, чтобы проверить, что курьер удален:
                given()
                .delete("/api/v1/courier/{courierId}", courierId)
                .then()
                .statusCode(404);
                System.out.println("Тестовый курьер с ID: " + courierId + " удалён.");
    }

    @Step("Delete Test Courier")
    public void clearTestData(Courier courier) {
        int courierID = sendPOSTRequestLoginAndGetCourierID(courier);
        sendDELETERequestCourierByID(courierID);
    }
}