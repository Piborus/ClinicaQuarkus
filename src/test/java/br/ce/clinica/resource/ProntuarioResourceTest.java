package br.ce.clinica.resource;

import br.ce.clinica.dto.request.ProntuarioRequest;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.dto.response.ProntuarioResponse;
import br.ce.clinica.dto.response.ProntuarioResumeResponse;
import br.ce.clinica.exception.ConflictBusinessException;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.service.ProntuarioService;
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
@DisplayName("ProntuarioResource Integration Tests")
class ProntuarioResourceTest {

    @InjectMock
    ProntuarioService prontuarioService;

    private ProntuarioRequest prontuarioRequest;
    private ProntuarioResponse prontuarioResponse;
    private ProntuarioResumeResponse prontuarioResumeResponse;

    @BeforeEach
    void setUp() {
        prontuarioRequest = ProntuarioRequest.builder()
                .texto("Texto do prontuário")
                .pacienteId(1L)
                .build();

        prontuarioResponse = ProntuarioResponse.builder()
                .id(1L)
                .texto("Texto do prontuário")
                .build();

        prontuarioResumeResponse = ProntuarioResumeResponse.builder()
                .id(1L)
                .texto("Texto do prontuário")
                .build();
    }

    @Test
    @DisplayName("Deve criar prontuário com sucesso")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void criarProntuarioComSucesso() {
        when(prontuarioService.save(any(ProntuarioRequest.class)))
                .thenReturn(Uni.createFrom().item(prontuarioResponse));

        given()
            .contentType(ContentType.JSON)
            .body(prontuarioRequest)
        .when()
            .post("/prontuario-do-paciente")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("texto", is("Texto do prontuário"));
    }

    @Test
    @DisplayName("Deve retornar erro ao criar prontuário para paciente inexistente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void criarProntuarioPacienteNaoEncontrado() {
        when(prontuarioService.save(any(ProntuarioRequest.class)))
                .thenReturn(Uni.createFrom().failure(new NotFoundBusinessException("Paciente nao encontrado")));

        given()
            .contentType(ContentType.JSON)
            .body(prontuarioRequest)
        .when()
            .post("/prontuario-do-paciente")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Deve retornar erro ao criar prontuário para paciente inativo")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void criarProntuarioPacienteInativo() {
        when(prontuarioService.save(any(ProntuarioRequest.class)))
                .thenReturn(Uni.createFrom().failure(new ConflictBusinessException("Paciente inativo")));

        given()
            .contentType(ContentType.JSON)
            .body(prontuarioRequest)
        .when()
            .post("/prontuario-do-paciente")
        .then()
            .statusCode(409);
    }

    @Test
    @DisplayName("Deve buscar prontuário por ID com sucesso")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void buscarProntuarioPorIdComSucesso() {
        when(prontuarioService.findById(1L))
                .thenReturn(Uni.createFrom().item(prontuarioResumeResponse));

        given()
        .when()
            .get("/prontuario-do-paciente/1")
        .then()
            .statusCode(200)
            .body("id", is(1))
            .body("texto", is("Texto do prontuário"));
    }

    @Test
    @DisplayName("Deve retornar erro ao buscar prontuário inexistente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void buscarProntuarioNaoEncontrado() {
        when(prontuarioService.findById(999L))
                .thenReturn(Uni.createFrom().failure(new NotFoundBusinessException("Prontuario nao encontrado")));

        given()
        .when()
            .get("/prontuario-do-paciente/999")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Deve deletar prontuário com sucesso")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void deletarProntuarioComSucesso() {
        when(prontuarioService.deleteById(1L))
                .thenReturn(Uni.createFrom().item(true));

        given()
        .when()
            .delete("/prontuario-do-paciente/1")
        .then()
            .statusCode(204);
    }

    @Test
    @DisplayName("Deve retornar erro ao deletar prontuário inexistente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void deletarProntuarioNaoEncontrado() {
        when(prontuarioService.deleteById(999L))
                .thenReturn(Uni.createFrom().failure(new NotFoundBusinessException("Prontuario do paciente nao encontrado")));

        given()
        .when()
            .delete("/prontuario-do-paciente/999")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Deve atualizar prontuário com sucesso")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void atualizarProntuarioComSucesso() {
        when(prontuarioService.update(eq(1L), any(ProntuarioRequest.class)))
                .thenReturn(Uni.createFrom().item(prontuarioResumeResponse));

        given()
            .contentType(ContentType.JSON)
            .body(prontuarioRequest)
        .when()
            .put("/prontuario-do-paciente/1")
        .then()
            .statusCode(200)
            .body("id", is(1))
            .body("texto", is("Texto do prontuário"));
    }

    @Test
    @DisplayName("Deve retornar erro ao atualizar prontuário inexistente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void atualizarProntuarioNaoEncontrado() {
        when(prontuarioService.update(eq(999L), any(ProntuarioRequest.class)))
                .thenReturn(Uni.createFrom().failure(new NotFoundBusinessException("Prontuario do paciente nao encontrado")));

        given()
            .contentType(ContentType.JSON)
            .body(prontuarioRequest)
        .when()
            .put("/prontuario-do-paciente/999")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Deve buscar prontuário com paciente associado")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void buscarProntuarioComPaciente() {
        when(prontuarioService.findByIdWithPaciente(1L))
                .thenReturn(Uni.createFrom().item(prontuarioResponse));

        given()
        .when()
            .get("/prontuario-do-paciente/1/paciente")
        .then()
            .statusCode(200)
            .body("id", is(1));
    }

    @Test
    @DisplayName("Deve listar prontuários paginados")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void listarProntuariosPaginados() {
        PanachePage<ProntuarioResponse> panachePage = PanachePage.<ProntuarioResponse>builder()
                .content(List.of(prontuarioResponse))
                .page(Page.of(0, 10))
                .totalCount(1L)
                .build();

        when(prontuarioService.findPaginated(any(Page.class), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(panachePage));

        given()
            .queryParam("page", 1)
            .queryParam("size", 10)
        .when()
            .get("/prontuario-do-paciente")
        .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("totalCount", is(1));
    }

}

