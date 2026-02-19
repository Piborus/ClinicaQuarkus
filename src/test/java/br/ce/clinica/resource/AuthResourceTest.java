package br.ce.clinica.resource;

import br.ce.clinica.dto.request.LoginRequest;
import br.ce.clinica.dto.request.UsuarioRequest;
import br.ce.clinica.dto.response.TokenResponse;
import br.ce.clinica.dto.response.UsuarioResponse;
import br.ce.clinica.exception.BadRequestBusinessException;
import br.ce.clinica.exception.UnauthorizedBusinessException;
import br.ce.clinica.service.AuthService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
@DisplayName("AuthResource Integration Tests")
class AuthResourceTest {

    @InjectMock
    AuthService authService;

    private UsuarioRequest usuarioRequest;
    private LoginRequest loginRequest;
    private UsuarioResponse usuarioResponse;
    private TokenResponse tokenResponse;

    @BeforeEach
    void setUp() {
        usuarioRequest = UsuarioRequest.builder()
                .nome("João da Silva")
                .email("joao@email.com")
                .senha("senha123")
                .build();

        loginRequest = LoginRequest.builder()
                .email("joao@email.com")
                .senha("senha123")
                .build();

        usuarioResponse = UsuarioResponse.builder()
                .id(1L)
                .nome("João da Silva")
                .email("joao@email.com")
                .build();

        tokenResponse = TokenResponse.builder()
                .accessToken("jwt.token.here")
                .build();
    }

    @Test
    @DisplayName("Deve cadastrar usuário com sucesso")
    void cadastrarUsuarioComSucesso() {
        when(authService.save(any(UsuarioRequest.class)))
                .thenReturn(Uni.createFrom().item(usuarioResponse));

        given()
            .contentType(ContentType.JSON)
            .body(usuarioRequest)
        .when()
            .post("/auth")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("nome", is("João da Silva"))
            .body("email", is("joao@email.com"));
    }

    @Test
    @DisplayName("Deve retornar erro ao cadastrar usuário com email já existente")
    void cadastrarUsuarioEmailJaExistente() {
        when(authService.save(any(UsuarioRequest.class)))
                .thenReturn(Uni.createFrom().failure(new BadRequestBusinessException("Email já cadastrado")));

        given()
            .contentType(ContentType.JSON)
            .body(usuarioRequest)
        .when()
            .post("/auth")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Deve realizar login com sucesso")
    void loginComSucesso() {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(Uni.createFrom().item(tokenResponse));

        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(201)
            .body("accessToken", notNullValue());
    }

    @Test
    @DisplayName("Deve retornar erro ao fazer login com credenciais inválidas")
    void loginCredenciaisInvalidas() {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(Uni.createFrom().failure(new UnauthorizedBusinessException("Usuário ou senha inválidos")));

        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("Deve retornar erro de validação ao cadastrar usuário sem nome")
    void cadastrarUsuarioSemNome() {
        usuarioRequest.setNome(null);

        given()
            .contentType(ContentType.JSON)
            .body(usuarioRequest)
        .when()
            .post("/auth")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Deve retornar erro de validação ao cadastrar usuário com email inválido")
    void cadastrarUsuarioEmailInvalido() {
        usuarioRequest.setEmail("email-invalido");

        given()
            .contentType(ContentType.JSON)
            .body(usuarioRequest)
        .when()
            .post("/auth")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Deve retornar erro de validação ao fazer login sem email")
    void loginSemEmail() {
        loginRequest.setEmail(null);

        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Deve retornar erro de validação ao fazer login sem senha")
    void loginSemSenha() {
        loginRequest.setSenha(null);

        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(400);
    }
}

