package br.ifsp.demo.integration.api;

import br.ifsp.demo.support.ApiTestBase;
import br.ifsp.demo.support.DbFixtures;
import br.ifsp.demo.support.FakeData;
import br.ifsp.demo.support.SeedData;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Tag("ApiTest")
@Tag("IntegrationTest")
@DisplayName("API de autenticação: /register, /authenticate, /auth/me e /hello")
class AuthApiTest extends ApiTestBase {
    @Test
    @DisplayName("retorna 201 com ids do usuário e do cliente ao cadastrar com dados válidos")
    void shouldRegisterStudentWithValidData() {
        Map<String, Object> body = FakeData.registerRequest();

        var response = anonymous().body(body).when().post("/register")
                .then().statusCode(201)
                .contentType(ContentType.JSON)
                .body("userId", notNullValue())
                .body("customerId", notNullValue())
                .extract();

        UUID userId = UUID.fromString(response.path("userId"));
        assertThat(DbFixtures.text("SELECT role FROM app_user WHERE id = ?", userId.toString())).isEqualTo("USER");
        assertThat(DbFixtures.text("SELECT user_id FROM customers WHERE id = ?", response.path("customerId").toString()))
                .isEqualTo(userId.toString());
    }

    @Test
    @DisplayName("grava o endereço quando ele é informado no cadastro")
    void shouldRegisterStudentWithAddress() {
        Map<String, Object> body = FakeData.registerRequest();
        Map<String, Object> address = FakeData.address();
        body.put("address", address);

        String customerId = anonymous().body(body).when().post("/register")
                .then().statusCode(201).extract().path("customerId");

        assertThat(DbFixtures.text("SELECT address_city FROM customers WHERE id = ?", customerId))
                .isEqualTo(address.get("city"));
    }

    @Test
    @DisplayName("aceita CPF com pontuação no cadastro e grava só os dígitos")
    void shouldNormalizeFormattedCpf() {
        Map<String, Object> body = FakeData.registerRequest();
        String cpf = (String) body.get("cpf");
        body.put("cpf", FakeData.formattedCpf(cpf));

        String customerId = anonymous().body(body).when().post("/register")
                .then().statusCode(201).extract().path("customerId");

        assertThat(DbFixtures.text("SELECT cpf FROM customers WHERE id = ?", customerId)).isEqualTo(cpf);
    }

    @Test
    @DisplayName("grava o e-mail do cadastro em minúsculas")
    void shouldNormalizeEmail() {
        Map<String, Object> body = FakeData.registerRequest();
        String email = (String) body.get("email");
        body.put("email", email.toUpperCase());

        String userId = anonymous().body(body).when().post("/register")
                .then().statusCode(201).extract().path("userId");

        assertThat(DbFixtures.text("SELECT email FROM app_user WHERE id = ?", userId)).isEqualTo(email.toLowerCase());
    }

    static Stream<Arguments> invalidFields() {
        return Stream.of(
                Arguments.of("name", "   "),
                Arguments.of("name", "a".repeat(101)),
                Arguments.of("lastname", ""),
                Arguments.of("email", "not-an-email"),
                Arguments.of("email", null),
                Arguments.of("password", "1234567"),
                Arguments.of("password", "a".repeat(73)),
                Arguments.of("cpf", "123"),
                Arguments.of("cpf", "abc.def.ghi-jk"),
                Arguments.of("birthdate", FakeData.futureDate().toString()),
                Arguments.of("birthdate", null),
                Arguments.of("gender", null),
                Arguments.of("tel", "1".repeat(21)));
    }

    @ParameterizedTest(name = "[{index}] {0} = {1}")
    @MethodSource("invalidFields")
    @DisplayName("retorna 400 e identifica o campo inválido no cadastro")
    void shouldRejectInvalidField(String field, Object value) {
        Map<String, Object> body = FakeData.registerRequest();
        body.put(field, value);

        anonymous().body(body).when().post("/register")
                .then().statusCode(400)
                .contentType(ContentType.JSON)
                .body("status", equalTo(400))
                .body("message", equalTo("Existem campos inválidos no cadastro."))
                .body("errors." + field, not(emptyOrNullString()));

        assertThat(DbFixtures.count("SELECT COUNT(*) FROM app_user")).isEqualTo(2);
    }

    @Test
    @DisplayName("retorna 400 no cadastro quando a UF do endereço não tem 2 letras")
    void shouldRejectInvalidAddressState() {
        Map<String, Object> body = FakeData.registerRequest();
        Map<String, Object> address = FakeData.address();
        address.put("state", "SAO");
        body.put("address", address);

        anonymous().body(body).when().post("/register")
                .then().statusCode(400)
                .body("errors.'address.state'", equalTo("Informe a sigla do estado com 2 letras."));
    }

    @Test
    @DisplayName("retorna 409 no cadastro quando o e-mail já existe, sem diferenciar maiúsculas")
    void shouldRejectDuplicatedEmail() {
        Map<String, Object> body = FakeData.registerRequest();
        body.put("email", SeedData.ADMIN_EMAIL.toUpperCase());

        anonymous().body(body).when().post("/register")
                .then().statusCode(409)
                .body("message", equalTo("Este e-mail já está cadastrado."));
    }

    @Test
    @DisplayName("retorna 409 no cadastro quando o CPF já está vinculado a uma conta")
    void shouldRejectDuplicatedCpf() {
        Map<String, Object> body = FakeData.registerRequest();
        body.put("cpf", SeedData.ANA_CPF);

        anonymous().body(body).when().post("/register")
                .then().statusCode(409)
                .body("message", equalTo("Este CPF já está associado a uma conta."));
    }

    @Test
    @DisplayName("vincula a conta ao cliente criado pela administração com o mesmo CPF e e-mail")
    void shouldLinkExistingCustomerWithoutAccount() {
        Map<String, Object> body = FakeData.registerRequest();
        UUID customerId = DbFixtures.insertCustomer("Cliente Balcao", (String) body.get("cpf"), (String) body.get("email"));

        anonymous().body(body).when().post("/register")
                .then().statusCode(201)
                .body("customerId", equalTo(customerId.toString()));
    }

    @Test
    @DisplayName("retorna 409 no cadastro quando o CPF pertence a cliente com outro e-mail")
    void shouldRejectCpfOfCustomerWithAnotherEmail() {
        Map<String, Object> body = FakeData.registerRequest();
        DbFixtures.insertCustomer("Cliente Balcao", (String) body.get("cpf"), FakeData.email());

        anonymous().body(body).when().post("/register")
                .then().statusCode(409)
                .body("message", equalTo("O CPF já está cadastrado com outro e-mail. Procure a administração."));
    }

    @Test
    @DisplayName("retorna 400 no cadastro para CPF com dígito verificador inválido")
    void shouldRejectCpfWithInvalidCheckDigits() {
        Map<String, Object> body = FakeData.registerRequest();
        body.put("cpf", "11111111111");

        anonymous().body(body).when().post("/register").then().statusCode(400);
    }

    @Test
    @DisplayName("retorna 409 (e nunca outro erro) para cadastros simultâneos com o mesmo e-mail")
    void shouldHandleSimultaneousRegistrations() throws Exception {
        String email = FakeData.email();
        int requests = 8;
        ExecutorService executor = Executors.newFixedThreadPool(requests);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> responses = new ArrayList<>();
        for (int i = 0; i < requests; i++) {
            Map<String, Object> body = FakeData.registerRequest();
            body.put("email", email);
            responses.add(executor.submit(() -> {
                start.await();
                return anonymous().body(body).when().post("/register").statusCode();
            }));
        }
        start.countDown();
        List<Integer> statusCodes = new ArrayList<>();
        for (Future<Integer> response : responses) {
            statusCodes.add(response.get(30, TimeUnit.SECONDS));
        }
        executor.shutdown();

        assertThat(statusCodes).containsOnly(201, 409);
        assertThat(statusCodes).filteredOn(code -> code == 201).hasSize(1);
        assertThat(DbFixtures.count("SELECT COUNT(*) FROM app_user WHERE email = ?", email)).isEqualTo(1);
    }

    @Test
    @DisplayName("retorna 400 no cadastro com JSON malformado")
    void shouldRejectMalformedJson() {
        anonymous().body("{\"name\": ").when().post("/register").then().statusCode(400);
    }

    @Test
    @DisplayName("retorna 200 e um JWT ao autenticar com credenciais válidas")
    void shouldAuthenticateAdmin() {
        anonymous().body(Map.of("username", SeedData.ADMIN_EMAIL, "password", SeedData.ADMIN_PASSWORD))
                .when().post("/authenticate")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("token", matchesPattern("^[\\w-]+\\.[\\w-]+\\.[\\w-]+$"));
    }

    @Test
    @DisplayName("autentica ignorando maiúsculas e espaços no e-mail")
    void shouldAuthenticateWithNormalizedEmail() {
        anonymous().body(Map.of("username", "  ADMIN@IFIT.COM ", "password", SeedData.ADMIN_PASSWORD))
                .when().post("/authenticate")
                .then().statusCode(200)
                .body("token", not(emptyOrNullString()));
    }

    @Test
    @DisplayName("autentica o aluno logo depois do cadastro")
    void shouldAuthenticateRegisteredStudent() {
        Student student = newStudent();

        assertThat(student.token()).isNotBlank();
    }

    @Test
    @DisplayName("retorna 401 ao autenticar com senha errada")
    void shouldRejectWrongPassword() {
        anonymous().body(Map.of("username", SeedData.ADMIN_EMAIL, "password", FakeData.password()))
                .when().post("/authenticate")
                .then().statusCode(401);
    }

    @Test
    @DisplayName("retorna 401 ao autenticar com e-mail desconhecido")
    void shouldRejectUnknownEmail() {
        anonymous().body(Map.of("username", FakeData.email(), "password", FakeData.password()))
                .when().post("/authenticate")
                .then().statusCode(401);
    }

    @Test
    @DisplayName("retorna 400 ao autenticar sem usuário e senha")
    void shouldRejectEmptyBody() {
        anonymous().body("{}").when().post("/authenticate").then().statusCode(400);
    }

    @Test
    @DisplayName("retorna os dados do administrador em /auth/me")
    void shouldReturnAdminData() {
        asAdmin().when().get("/auth/me")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("userId", equalTo(SeedData.ADMIN_USER_ID.toString()))
                .body("customerId", equalTo(SeedData.ADMIN_CUSTOMER_ID.toString()))
                .body("email", equalTo(SeedData.ADMIN_EMAIL))
                .body("role", equalTo("ADMIN"));
    }

    @Test
    @DisplayName("retorna os dados do aluno cadastrado em /auth/me")
    void shouldReturnStudentData() {
        Student student = newStudent();

        as(student).when().get("/auth/me")
                .then().statusCode(200)
                .body("userId", equalTo(student.userId().toString()))
                .body("customerId", equalTo(student.customerId().toString()))
                .body("email", equalTo(student.email()))
                .body("role", equalTo("USER"));
    }

    @Test
    @DisplayName("retorna 401 em /auth/me sem token")
    void shouldRequireTokenOnMe() {
        anonymous().when().get("/auth/me").then().statusCode(401);
    }

    @Test
    @DisplayName("retorna 401 em /auth/me com token inválido")
    void shouldRejectInvalidToken() {
        as("invalid.token.value").when().get("/auth/me")
                .then().statusCode(401)
                .body("error", equalTo("JWT is not valid or is expired."));
    }

    @Test
    @DisplayName("cumprimenta o usuário autenticado com o id em /hello")
    void shouldGreetAuthenticatedUser() {
        asAdmin().when().get("/hello")
                .then().statusCode(200)
                .body(equalTo("Hello: " + SeedData.ADMIN_USER_ID));
    }

    @Test
    @DisplayName("retorna 401 em /hello sem token")
    void shouldRequireTokenOnHello() {
        anonymous().when().get("/hello").then().statusCode(401);
    }

    @Test
    @DisplayName("retorna 401 para rota inexistente sem autenticação")
    void shouldDenyUnknownEndpoint() {
        anonymous().when().get("/unknown-" + LocalDate.now()).then().statusCode(401);
    }
}
