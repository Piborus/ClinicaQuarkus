package br.ce.clinica.resource;

import br.ce.clinica.dto.request.FiliacaoRequest;
import br.ce.clinica.dto.response.FiliacaoResponse;
import br.ce.clinica.enums.GrauParentesco;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.service.FiliacaoService;
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
@DisplayName("FiliacaoResource Integration Tests")
class FiliacaoResourceTest {

    @InjectMock
    FiliacaoService filiacaoService;

    private FiliacaoRequest filiacaoRequest;
    private FiliacaoResponse filiacaoResponse;

    @BeforeEach
    void setUp() {
        filiacaoRequest = FiliacaoRequest.builder()
                .nome("Maria da Silva")
                .cpf("529.982.247-25")
                .telefone("11888888888")
                .email("maria@email.com")
                .grauDeParentesco(GrauParentesco.GENITOR)
                .build();

        filiacaoResponse = FiliacaoResponse.builder()
                .id(1L)
                .nome("Maria da Silva")
                .cpf("529.982.247-25")
                .telefone("11888888888")
                .email("maria@email.com")
                .grauDeParentesco(GrauParentesco.GENITOR)
                .build();
    }

    @Test
    @DisplayName("Deve buscar filiações por paciente ID com sucesso")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void buscarFiliacoesPorPacienteIdComSucesso() {
        when(filiacaoService.findByPacienteId(1L))
                .thenReturn(Uni.createFrom().item(List.of(filiacaoResponse)));

        given()
        .when()
            .get("/filiacoes/paciente/1")
        .then()
            .statusCode(200)
            .body("size()", is(1))
            .body("[0].nome", is("Maria da Silva"));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando paciente não tem filiações")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void buscarFiliacoesPorPacienteSemFiliacoes() {
        when(filiacaoService.findByPacienteId(1L))
                .thenReturn(Uni.createFrom().item(List.of()));

        given()
        .when()
            .get("/filiacoes/paciente/1")
        .then()
            .statusCode(200)
            .body("size()", is(0));
    }

    @Test
    @DisplayName("Deve retornar erro ao buscar filiações de paciente inexistente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void buscarFiliacoesPacienteNaoEncontrado() {
        when(filiacaoService.findByPacienteId(999L))
                .thenReturn(Uni.createFrom().failure(new NotFoundBusinessException("Paciente nao encontrado")));

        given()
        .when()
            .get("/filiacoes/paciente/999")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Deve atualizar filiação com sucesso")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void atualizarFiliacaoComSucesso() {
        when(filiacaoService.update(eq(1L), any(FiliacaoRequest.class)))
                .thenReturn(Uni.createFrom().item(filiacaoResponse));

        given()
            .contentType(ContentType.JSON)
            .body(filiacaoRequest)
        .when()
            .put("/filiacoes/1")
        .then()
            .statusCode(200)
            .body("id", is(1))
            .body("nome", is("Maria da Silva"));
    }

    @Test
    @DisplayName("Deve retornar erro ao atualizar filiação inexistente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void atualizarFiliacaoNaoEncontrada() {
        when(filiacaoService.update(eq(999L), any(FiliacaoRequest.class)))
                .thenReturn(Uni.createFrom().failure(new NotFoundBusinessException("Filiacao nao encontrada")));

        given()
            .contentType(ContentType.JSON)
            .body(filiacaoRequest)
        .when()
            .put("/filiacoes/999")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Deve buscar filiações com role ADMINISTRADOR")
    @TestSecurity(user = "admin", roles = {"ADMINISTRADOR"})
    void buscarFiliacoesComoAdministrador() {
        when(filiacaoService.findByPacienteId(1L))
                .thenReturn(Uni.createFrom().item(List.of(filiacaoResponse)));

        given()
        .when()
            .get("/filiacoes/paciente/1")
        .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("Deve buscar múltiplas filiações de um paciente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void buscarMultiplasFiliacoes() {
        FiliacaoResponse filiacaoResponse2 = FiliacaoResponse.builder()
                .id(2L)
                .nome("José da Silva")
                .cpf("052.684.917-77")
                .telefone("11777777777")
                .email("jose@email.com")
                .grauDeParentesco(GrauParentesco.GENITOR)
                .build();

        when(filiacaoService.findByPacienteId(1L))
                .thenReturn(Uni.createFrom().item(List.of(filiacaoResponse, filiacaoResponse2)));

        given()
        .when()
            .get("/filiacoes/paciente/1")
        .then()
            .statusCode(200)
            .body("size()", is(2))
            .body("[0].nome", is("Maria da Silva"))
            .body("[1].nome", is("José da Silva"));
    }
}

