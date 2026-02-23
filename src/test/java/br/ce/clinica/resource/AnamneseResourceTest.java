package br.ce.clinica.resource;

import br.ce.clinica.dto.request.AnamneseRequest;
import br.ce.clinica.dto.response.AnamneseResponse;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.exception.ConflictBusinessException;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.service.AnamneseService;
import io.quarkus.panache.common.Page;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@QuarkusTest
@DisplayName("AnamneseResource Integration Tests")
class AnamneseResourceTest {

    @InjectMock
    AnamneseService anamneseService;

    private AnamneseRequest anamneseRequest;
    private AnamneseResponse anamneseResponse;

    @BeforeEach
    void setUp() {
        anamneseRequest = AnamneseRequest.builder()
                .historicoAcompanhamento("Paciente relata dores de cabeça há 3 meses")
                .encaminhamento("Hospital")
                .pacienteId(1L)
                .build();

        anamneseResponse = AnamneseResponse.builder()
                .id(1L)
                .historicoAcompanhamento("Paciente relata dores de cabeça há 3 meses")
                .encaminhamento("Hospital")
                .build();
    }

    @Test
    @DisplayName("Deve criar anamnese com sucesso")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void criarAnamneseComSucesso() {
        when(anamneseService.save(any(AnamneseRequest.class)))
                .thenReturn(Uni.createFrom().item(anamneseResponse));

        given()
            .contentType(ContentType.JSON)
            .body(anamneseRequest)
        .when()
            .post("/anamnese")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("historicoAcompanhamento", is("Paciente relata dores de cabeça há 3 meses"));
    }

    @Test
    @DisplayName("Deve retornar erro ao criar anamnese para paciente inexistente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void criarAnamnesePacienteNaoEncontrado() {
        when(anamneseService.save(any(AnamneseRequest.class)))
                .thenReturn(Uni.createFrom().failure(new NotFoundBusinessException("Paciente nao encontrado")));

        given()
            .contentType(ContentType.JSON)
            .body(anamneseRequest)
        .when()
            .post("/anamnese")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Deve retornar erro ao criar anamnese para paciente inativo")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void criarAnamnesePacienteInativo() {
        when(anamneseService.save(any(AnamneseRequest.class)))
                .thenReturn(Uni.createFrom().failure(new ConflictBusinessException("Paciente inativo")));

        given()
            .contentType(ContentType.JSON)
            .body(anamneseRequest)
        .when()
            .post("/anamnese")
        .then()
            .statusCode(409);
    }

    @Test
    @DisplayName("Deve buscar anamnese por ID com sucesso")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void buscarAnamnesePorIdComSucesso() {
        when(anamneseService.findById(1L))
                .thenReturn(Uni.createFrom().item(anamneseResponse));

        given()
        .when()
            .get("/anamnese/1")
        .then()
            .statusCode(200)
            .body("id", is(1))
            .body("historicoAcompanhamento", is("Paciente relata dores de cabeça há 3 meses"));
    }

    @Test
    @DisplayName("Deve retornar erro ao buscar anamnese inexistente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void buscarAnamneseNaoEncontrada() {
        when(anamneseService.findById(999L))
                .thenReturn(Uni.createFrom().failure(new NotFoundBusinessException("Anamnese nao encontrada")));

        given()
        .when()
            .get("/anamnese/999")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Deve atualizar anamnese com sucesso")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void atualizarAnamneseComSucesso() {
        when(anamneseService.update(eq(1L), any(AnamneseRequest.class)))
                .thenReturn(Uni.createFrom().item(anamneseResponse));

        given()
            .contentType(ContentType.JSON)
            .body(anamneseRequest)
        .when()
            .put("/anamnese/1")
        .then()
            .statusCode(200)
            .body("id", is(1));
    }

    @Test
    @DisplayName("Deve retornar erro ao atualizar anamnese inexistente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void atualizarAnamneseNaoEncontrada() {
        when(anamneseService.update(eq(999L), any(AnamneseRequest.class)))
                .thenReturn(Uni.createFrom().failure(new NotFoundBusinessException("Anamnese nao encontrada")));

        given()
            .contentType(ContentType.JSON)
            .body(anamneseRequest)
        .when()
            .put("/anamnese/999")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Deve listar anamneses paginadas")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void listarAnamnesesPaginadas() {
        PanachePage<AnamneseResponse> panachePage = PanachePage.<AnamneseResponse>builder()
                .content(List.of(anamneseResponse))
                .page(Page.of(0, 10))
                .totalCount(1L)
                .build();

        when(anamneseService.findPaginated(any(Page.class), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(panachePage));

        given()
            .queryParam("page", 1)
            .queryParam("size", 10)
        .when()
            .get("/anamnese")
        .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("totalCount", is(1));
    }

    @Test
    @DisplayName("Deve listar anamneses com filtros e ordenação")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void listarAnamnesesComFiltrosEOrdenacao() {
        PanachePage<AnamneseResponse> panachePage = PanachePage.<AnamneseResponse>builder()
                .content(List.of(anamneseResponse))
                .page(Page.of(0, 10))
                .totalCount(1L)
                .build();

        when(anamneseService.findPaginated(any(Page.class), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(panachePage));

        given()
            .queryParam("page", 1)
            .queryParam("size", 10)
            .queryParam("sort", "id,desc")
            .queryParam("filterFields", "queixaPrincipal")
            .queryParam("filterValues", "dor")
        .when()
            .get("/anamnese")
        .then()
            .statusCode(200)
            .body("content", notNullValue());
    }


    @Test
    @DisplayName("Deve retornar erro de validação ao criar anamnese sem paciente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void criarAnamneseSemPaciente() {
        anamneseRequest.setPacienteId(null);

        given()
            .contentType(ContentType.JSON)
            .body(anamneseRequest)
        .when()
            .post("/anamnese")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Deve criar anamnese com role ADMINISTRADOR")
    @TestSecurity(user = "admin", roles = {"ADMINISTRADOR"})
    void criarAnamneseComoAdministrador() {
        when(anamneseService.save(any(AnamneseRequest.class)))
                .thenReturn(Uni.createFrom().item(anamneseResponse));

        given()
            .contentType(ContentType.JSON)
            .body(anamneseRequest)
        .when()
            .post("/anamnese")
        .then()
            .statusCode(201);
    }
}



