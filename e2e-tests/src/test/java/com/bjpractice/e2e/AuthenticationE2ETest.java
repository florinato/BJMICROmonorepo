package com.bjpractice.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class AuthenticationE2ETest {


    // ARRANGE

    @BeforeAll
    static void setup(){

        RestAssured.baseURI = "http://apisix:9080";
    }

    @Test
    @DisplayName("Debe devolver un token JWT al hacer login con credenciales válidas")
    void shouldReturnJwtToken_WhenLoginWithValidCredentials() {

        String loginPayload = """
        {
            "username": "testuser",
            "password": "password123"
        }
        """;

        given()
                .contentType(ContentType.JSON) // Decimos que enviaremos un JSON
                .body(loginPayload)            // Adjuntamos el cuerpo de la petición

                .when()
                .post("/api/auth/login")       // Hacemos la petición POST al endpoint de login

                .then()
                .statusCode(200)               // 1. Verificamos que el código de estado es 200 OK
                .body("token", notNullValue());
    }
}
