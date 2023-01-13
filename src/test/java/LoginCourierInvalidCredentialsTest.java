import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class LoginCourierInvalidCredentialsTest {

    private String login;
    private String password;

    public LoginCourierInvalidCredentialsTest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Parameterized.Parameters
    public static Object[][] getTestData() {
        return new Object[][]{
                {"Auto0203", "qwerty123"},
                {"Auto0203", "QwEry123"},
                {"Auto0203", "QwErty1233"},
                {"auto0203", "QwErty123"},
                {"Aut0203", "QwErty123"},
                {"AAuto0203", "QwErty123"},
        };
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }


    @Test
    @DisplayName("POST /courier/login: check response for invalid credentials")
    @Description("Check login fails if credentials are invalid: code '404' with error message")
    public void checkCourierLoginInvalidCredentials() {
        Courier courier = new Courier("Auto0203", "QwErty123", "Marina");
        createTestCourier(courier);
        Response response1 = sendPOSTRequestLogin(login, password);
        checkPOSTResponseLoginIs404(response1);
        Response response2 = sendPOSTRequestLogin(courier.getLogin(), courier.getPassword());
        int courierId = checkPOSTResponseLoginIs200(response2);
        clearTestData(courierId);
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
    public Response sendPOSTRequestLogin(String login, String password) {
        CourierCredentials credentials = new CourierCredentials(login, password);
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