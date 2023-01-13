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
public class AddCourierMandatoryFieldsTest {

    private String login;
    private String password;
    private String firstName;

    public AddCourierMandatoryFieldsTest(String login, String password, String firstName) {
        this.login = login;
        this.password = password;
        this.firstName = firstName;
    }

    @Parameterized.Parameters
    public static Object[][] getTestData() {
        return new Object[][]{
                {null, "qwerty1234", "Marina"},
                {"auto0105", null, "Marina"},
                {"auto0106", "qwerty1234", null},
                {null, null, null},
                {null, null, "Marina"},
                {null, "qwerty1234", null},
                {"auto0107", null, null},
                {"", "qwerty1234", "Marina"},
                {"auto0108", "", "Marina"},
                {"auto0109", "qwerty1234", ""},
                {"", "", ""},
                {"", "", "Marina"},
                {"", "qwerty1234", ""},
                {"auto0110", "", ""},
        };
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("POST /courier: mandatory fields check")
    @Description("Check that POST /courier endpoint requires all mandatory fields to be sent")
    public void checkCourierCreationMandatoryFields() {
        Courier courier = new Courier(login, password, firstName);
        Response response = sendPOSTRequestCourier(courier);
        checkPOSTResponseCourierIs400(response);
    }

    @Step("Send POST /courier request")
    public Response sendPOSTRequestCourier(Courier courier) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(courier);
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(json)
                        .when()
                        .post("/api/v1/courier");
        return response;
    }

    @Step("Check POST /courier response is '400'")
    public void checkPOSTResponseCourierIs400(Response response) {
        response.then().assertThat().body("message", equalTo("Недостаточно данных для создания учетной записи"))
                .and()
                .statusCode(400);
    }
}
