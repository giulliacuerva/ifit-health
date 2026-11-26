package br.ifsp.demo.support;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public abstract class ApiTestBase extends IntegrationTestBase {
    protected record Student(UUID userId, UUID customerId, String email, String password, String token) {
    }

    protected static Matcher<Object> isNumber(double expected) {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(Object actual) {
                return actual instanceof Number number && Math.abs(number.doubleValue() - expected) < 0.001;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("um número igual a ").appendValue(expected);
            }
        };
    }

    private RequestSpecification api;

    @BeforeEach
    void configureRestAssured() {
        api = new RequestSpecBuilder()
                .setPort(port)
                .setBasePath("/api/v1")
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    protected RequestSpecification anonymous() {
        return given().spec(api);
    }

    protected RequestSpecification as(String token) {
        return anonymous().header("Authorization", "Bearer " + token);
    }

    protected RequestSpecification asAdmin() {
        return as(adminToken());
    }

    protected RequestSpecification as(Student student) {
        return as(student.token());
    }

    protected String login(String email, String password) {
        return anonymous()
                .body(Map.of("username", email, "password", password))
                .post("/authenticate")
                .then().statusCode(200)
                .extract().path("token");
    }

    protected String adminToken() {
        return login(SeedData.ADMIN_EMAIL, SeedData.ADMIN_PASSWORD);
    }

    protected Student newStudent() {
        Map<String, Object> body = FakeData.registerRequest();
        var response = anonymous().body(body).post("/register").then().statusCode(201).extract();
        String email = (String) body.get("email");
        String password = (String) body.get("password");
        return new Student(
                UUID.fromString(response.path("userId")),
                UUID.fromString(response.path("customerId")),
                email,
                password,
                login(email, password));
    }
}
