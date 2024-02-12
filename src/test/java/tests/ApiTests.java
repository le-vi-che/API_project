package tests;

import config.apiConfig;
import io.qameta.allure.Owner;
import models.*;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static specs.CreateSpec.createUserRequestSpec;
import static specs.CreateSpec.createUserResponseSpec;
import static specs.DeleteSpec.responseSpecWithStatusCode204;
import static specs.RegisterSpec.*;
import static org.assertj.core.api.Assertions.assertThat;

@Owner("emonovaev")
public class ApiTests {

    private static apiConfig config = ConfigFactory.create(apiConfig.class);

    @Test
    @DisplayName("Успешное создание нового пользователя")
    void successfulCreateUserTest() {

        CreateUserBodyModel userData = new CreateUserBodyModel();
        userData.setName("bond");
        userData.setJob("qa");

        CreateUserResponseModel response = step("Запрос на создание нового пользователя", ()->
                given(createUserRequestSpec)
                        .body(userData)

                        .when()
                        .post()

                        .then()
                        .spec(createUserResponseSpec)
                        .extract().as(CreateUserResponseModel.class));

        step("Проверка имени нового пользователя", ()->
                assertEquals("bond",response.getName()));

        step("Проверка профессии нового пользователя", ()->
                assertEquals("qa", response.getJob()));

        step("Проверка присвоения id пользователю", ()->
                assertNotNull(response.getId()));

        step("Проверка записи времени создания пользователя", ()->
                assertNotNull(response.getCreatedAt()));
    }

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void successfulRegisterUserTest() {

        RegistrationBodyModel authData = new RegistrationBodyModel();
        authData.setEmail(config.authEmail());
        authData.setPassword(config.authPassword());

        RegistrationResponseModel response = step("Запрос на регистрацию существующего пользователя", ()->
         given(registerRequestSpec)
                .body(authData)

                .when()
                .post()

                .then()
                .spec(responseSpec)
                .extract().as(RegistrationResponseModel.class));

        step("Проверка Id", ()->
                assertNotNull(response.getId()));

        step("Проверка token", ()->
                assertNotNull(response.getToken()));
}

    @Test
    @DisplayName("Отправка на регистрацию с незаполненными email/password")
    void emptyAuthDataTest() {

        Error400Model response = step("Передача запроса на регистрацию с незаполненными email/password", ()->
        given(registerRequestSpec)
                .when()
                .post()

                .then()
                .spec(errorResponseSpec)
                .extract().as(Error400Model.class));

                step("Проверка ответа", ()->
                        assertEquals("Missing email or username", response.getError()));
    }

    @Test
    @DisplayName("Обновление данных пользователя")
    void putUserTest() {
        UpdateUserBodyModel updateBody = new UpdateUserBodyModel();
        updateBody.setName("JamesBond");
        updateBody.setJob("Agent 007");
        PutResponseUserModel responseUpdateUser = step("Обновляем Name и Job пользователя", () ->
                given(registerRequestSpec)
                        .body(updateBody)
                        .put("/users/2/")
                        .then()
                        .spec(responseSpec)
                        .extract().as(PutResponseUserModel.class));

        step("Проверяем изменения", () -> {
            assertThat("JamesBond").isEqualTo(responseUpdateUser.getName());
            assertThat("Agent 007").isEqualTo(responseUpdateUser.getJob());
            assertThat(responseUpdateUser.getUpdatedAt()).isNotNull();
        });
    }

    @Test
    @DisplayName("Удаление пользователя")
    void deleteUserTest() {
        step("Проверяем статус удаления пользователя", () -> {
            given(registerRequestSpec)
                    .delete("/users/2")
                    .then()
                    .log().body()
                    .spec(responseSpecWithStatusCode204);
        });
    }

    }

