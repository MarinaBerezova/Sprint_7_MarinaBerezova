import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

@RunWith(Parameterized.class)
public class LoginCourierMandatoryFieldsTest {

    private String login;
    private String password;

    public LoginCourierMandatoryFieldsTest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Parameterized.Parameters
    public static Object[][] getTestData() {
        return new Object[][]{
                {null, "password"},
                {"tester", null},
                {null, null},
                {"tester", ""},
                {"", "password"},
                {"", ""},
        };
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("POST /courier/login: check mandatory fields")
    @Description("Check that POST /courier/login endpoint requires all mandatory fields to be sent")
    public void checkCourierLoginMandatoryFields() {
        Response response = sendPOSTRequestLogin(login, password);
        checkPOSTResponseLoginIs400(response);
    }

    @Step("Send POST /courier/login request")
    public Response sendPOSTRequestLogin(String login, String password) {
        CourierCredentials credentials = new CourierCredentials(login, password);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(credentials);
        Response response =
                given()
                        .header("Content-Type", "application/json")
                        .body(json)
                        .when()
                        .post("/api/v1/courier/login");
        return response;
    }

    @Step("Check POST /courier/login returns '400' if mandatory fields are missing in request")
    public void checkPOSTResponseLoginIs400(Response response) {
        response.then().assertThat().body("message", equalTo("Недостаточно данных для входа"))
                .and()
                .statusCode(400);
    }
}