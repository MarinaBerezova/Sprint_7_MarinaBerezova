import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class LoginCourierTest {

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Basic test for POST /courier/login request")
    @Description("Check successful courier login response: code '200' and 'id'")
    public void checkCourierLoginSuccess(){
        Courier courier = new Courier("auto0201", "qwerty1234", "Marina");
        createTestCourier(courier);
        Response response = sendPOSTRequestLogin(courier);
        int courierId = checkPOSTResponseLoginIs200(response);
        clearTestData(courierId);
    }

    @Test
    @DisplayName("POST /courier/login: check response for non-existing courier")
    @Description("Check login fails for non-existing courier: code '404' with error message")
    public void checkCourierLoginFail(){
        Courier courier = new Courier("auto0202", "123test", "Anna");
        createTestCourier(courier);
        Response response1 = sendPOSTRequestLogin(courier);
        int courierId = checkPOSTResponseLoginIs200(response1);
        clearTestData(courierId);
        Response response2 = sendPOSTRequestLogin(courier);
        checkPOSTResponseLoginIs404(response2);
    }

    @Step("Send POST /courier request to create courier for test")
    public void createTestCourier(Courier courier) {
            given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(courier)
                        .when()
                        .post("/api/v1/courier")
                        .then().statusCode(201);
    }

    @Step("Send POST /courier/login request")
    public Response sendPOSTRequestLogin(Courier courier) {
        CourierCredentials credentials = new CourierCredentials(courier.getLogin(), courier.getPassword());
        Response response =
                given()
                        .header("Content-Type", "application/json")
                        .body(credentials)
                        .when()
                        .post("/api/v1/courier/login");
        return response;
    }

    @Step("Check POST /courier/login returns code '200 OK' and courier 'Id'")
    public int checkPOSTResponseLoginIs200(Response response) {
       int courierId =
               response.then()
                       .body("id", notNullValue())
                       .statusCode(200)
                       .extract()
                       .path("id");
        return courierId;
    }


    @Step("Check POST /courier/login returns '404' for invalid credentials")
    public void checkPOSTResponseLoginIs404(Response response) {
        response.then().assertThat().body("message", equalTo("Учетная запись не найдена"))
                .and()
                .statusCode(404);
    }


    @Step("Send DELETE /courier/{id} to remove test data")
    public void clearTestData(int courierId) {
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
}
