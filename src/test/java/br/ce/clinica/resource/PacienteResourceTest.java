package br.ce.clinica.resource;

import br.ce.clinica.dto.request.EnderecoRequest;
import br.ce.clinica.dto.request.PacienteRequest;
import br.ce.clinica.dto.response.PacienteResponse;
import br.ce.clinica.dto.response.PacienteResumeResponse;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.enums.Sexo;
import br.ce.clinica.exception.ConflictBusinessException;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.exception.UnprocessableEntityBusinessException;
import br.ce.clinica.service.PacienteService;
import io.quarkus.panache.common.Page;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@QuarkusTest
@DisplayName("PacienteResource Integration Tests")
class PacienteResourceTest {

    @InjectMock
    PacienteService pacienteService;

    private PacienteRequest pacienteRequest;
    private PacienteResponse pacienteResponse;
    private PacienteResumeResponse pacienteResumeResponse;

    private static final String CPF_VALIDO = "089.703.540-26";

    @BeforeEach
    void setUp() {
        pacienteRequest = PacienteRequest.builder()
                .nome("João da Silva")
                .cpf(CPF_VALIDO)
                .rg("123456789")
                .dataNascimento(LocalDate.of(1990, 1, 1))
                .sexo(Sexo.MASCULINO)
                .telefone("11999999999")
                .email("joao@email.com")
                .idade(34)
                .build();

        pacienteResponse = PacienteResponse.builder()
                .id(1L)
                .nome("João da Silva")
                .cpf(CPF_VALIDO)
                .rg("123456789")
                .dataNascimento(LocalDate.of(1990, 1, 1))
                .sexo(Sexo.MASCULINO)
                .telefone("11999999999")
                .email("joao@email.com")
                .idade(34)
                .responsaveis(List.of())
                .transacoes(List.of())
                .prontuarios(List.of())
                .build();

        pacienteResumeResponse = PacienteResumeResponse.builder()
                .id(1L)
                .nome("João da Silva")
                .cpf(CPF_VALIDO)
                .build();
    }

    @Test
    @DisplayName("Deve criar paciente com sucesso")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void criarPacienteComSucesso() {
        when(pacienteService.save(any(PacienteRequest.class)))
                .thenReturn(Uni.createFrom().item(pacienteResponse));

        given()
            .contentType(ContentType.JSON)
            .body(pacienteRequest)
        .when()
            .post("/pacientes")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("nome", is("João da Silva"))
            .body("cpf", is(CPF_VALIDO));
    }

    @Test
    @DisplayName("Deve criar paciente com endereço")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void criarPacienteComEndereco() {
        EnderecoRequest enderecoRequest = EnderecoRequest.builder()
                .logradouro("Rua Teste")
                .numero("123")
                .bairro("Centro")
                .cep("12345-678")
                .cidade("São Paulo")
                .estado("SP")
                .pais("Brasil")
                .build();
        pacienteRequest.setEndereco(enderecoRequest);

        when(pacienteService.save(any(PacienteRequest.class)))
                .thenReturn(Uni.createFrom().item(pacienteResponse));

        given()
            .contentType(ContentType.JSON)
            .body(pacienteRequest)
        .when()
            .post("/pacientes")
        .then()
            .statusCode(201)
            .body("id", notNullValue());
    }

    @Test
    @DisplayName("Deve retornar erro ao criar paciente com CPF já existente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void criarPacienteCpfJaExistente() {
        when(pacienteService.save(any(PacienteRequest.class)))
                .thenReturn(Uni.createFrom().failure(new ConflictBusinessException("CPF ja existente!")));

        given()
            .contentType(ContentType.JSON)
            .body(pacienteRequest)
        .when()
            .post("/pacientes")
        .then()
            .statusCode(409);
    }

    @Test
    @DisplayName("Deve retornar erro ao criar paciente com RG já existente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void criarPacienteRgJaExistente() {
        when(pacienteService.save(any(PacienteRequest.class)))
                .thenReturn(Uni.createFrom().failure(new ConflictBusinessException("RG ja existente!")));

        given()
            .contentType(ContentType.JSON)
            .body(pacienteRequest)
        .when()
            .post("/pacientes")
        .then()
            .statusCode(409);
    }

    @Test
    @DisplayName("Deve buscar paciente por ID com sucesso")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void buscarPacientePorIdComSucesso() {
        when(pacienteService.findById(1L))
                .thenReturn(Uni.createFrom().item(pacienteResponse));

        given()
        .when()
            .get("/pacientes/1")
        .then()
            .statusCode(200)
            .body("id", is(1))
            .body("nome", is("João da Silva"));
    }

    @Test
    @DisplayName("Deve retornar erro ao buscar paciente inexistente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void buscarPacienteNaoEncontrado() {
        when(pacienteService.findById(999L))
                .thenReturn(Uni.createFrom().failure(new NotFoundBusinessException("Paciente não encontrado!")));

        given()
        .when()
            .get("/pacientes/999")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Deve deletar paciente com sucesso")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void deletarPacienteComSucesso() {
        when(pacienteService.softDelete(1L))
                .thenReturn(Uni.createFrom().item(true));

        given()
        .when()
            .patch("/pacientes/delete/1")
        .then()
            .statusCode(204);
    }

    @Test
    @DisplayName("Deve retornar erro ao deletar paciente inexistente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void deletarPacienteNaoEncontrado() {
        when(pacienteService.softDelete(999L))
                .thenReturn(Uni.createFrom().failure(new NotFoundBusinessException("Paciente não encontrado")));

        given()
        .when()
            .patch("/pacientes/delete/999")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Deve retornar erro ao deletar paciente já arquivado")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void deletarPacienteJaArquivado() {
        when(pacienteService.softDelete(1L))
                .thenReturn(Uni.createFrom().failure(new UnprocessableEntityBusinessException("Paciente já arquivado")));

        given()
        .when()
            .patch("/pacientes/delete/1")
        .then()
            .statusCode(422);
    }

    @Test
    @DisplayName("Deve restaurar paciente com sucesso")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void restaurarPacienteComSucesso() {
        when(pacienteService.restore(1L))
                .thenReturn(Uni.createFrom().item(true));

        given()
        .when()
            .patch("/pacientes/restore/1")
        .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("Deve retornar erro ao restaurar paciente inexistente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void restaurarPacienteNaoEncontrado() {
        when(pacienteService.restore(999L))
                .thenReturn(Uni.createFrom().failure(new NotFoundBusinessException("Paciente não encontrado")));

        given()
        .when()
            .patch("/pacientes/restore/999")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Deve retornar erro ao restaurar paciente já ativo")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void restaurarPacienteJaAtivo() {
        when(pacienteService.restore(1L))
                .thenReturn(Uni.createFrom().failure(new ConflictBusinessException("Paciente já ativo")));

        given()
        .when()
            .patch("/pacientes/restore/1")
        .then()
            .statusCode(409);
    }

    @Test
    @DisplayName("Deve atualizar paciente com sucesso")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void atualizarPacienteComSucesso() {
        when(pacienteService.update(eq(1L), any(PacienteRequest.class)))
                .thenReturn(Uni.createFrom().item(pacienteResumeResponse));

        given()
            .contentType(ContentType.JSON)
            .body(pacienteRequest)
        .when()
            .put("/pacientes/1")
        .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("Deve retornar erro ao atualizar paciente inexistente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void atualizarPacienteNaoEncontrado() {
        when(pacienteService.update(eq(999L), any(PacienteRequest.class)))
                .thenReturn(Uni.createFrom().failure(new NotFoundBusinessException("Paciente não encontrado")));

        given()
            .contentType(ContentType.JSON)
            .body(pacienteRequest)
        .when()
            .put("/pacientes/999")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Deve retornar erro ao atualizar paciente arquivado")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void atualizarPacienteArquivado() {
        when(pacienteService.update(eq(1L), any(PacienteRequest.class)))
                .thenReturn(Uni.createFrom().failure(new UnprocessableEntityBusinessException("Paciente arquivado, não é possível atualizar!")));

        given()
            .contentType(ContentType.JSON)
            .body(pacienteRequest)
        .when()
            .put("/pacientes/1")
        .then()
            .statusCode(422);
    }

    @Test
    @DisplayName("Deve listar pacientes paginados")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void listarPacientesPaginados() {
        PanachePage<PacienteResponse> panachePage = PanachePage.<PacienteResponse>builder()
                .content(List.of(pacienteResponse))
                .page(Page.of(0, 10))
                .totalCount(1L)
                .build();

        when(pacienteService.findPaginated(any(Page.class), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(panachePage));

        given()
            .queryParam("page", 1)
            .queryParam("size", 10)
        .when()
            .get("/pacientes")
        .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("totalCount", is(1));
    }

    @Test
    @DisplayName("Deve listar pacientes com filtros e ordenação")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void listarPacientesComFiltrosEOrdenacao() {
        PanachePage<PacienteResponse> panachePage = PanachePage.<PacienteResponse>builder()
                .content(List.of(pacienteResponse))
                .page(Page.of(0, 10))
                .totalCount(1L)
                .build();

        when(pacienteService.findPaginated(any(Page.class), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(panachePage));

        given()
            .queryParam("page", 1)
            .queryParam("size", 10)
            .queryParam("sort", "nome,asc")
            .queryParam("filterFields", "nome")
            .queryParam("filterValues", "João")
        .when()
            .get("/pacientes")
        .then()
            .statusCode(200)
            .body("content", notNullValue());
    }


    @Test
    @DisplayName("Deve criar paciente com role ADMINISTRADOR")
    @TestSecurity(user = "admin", roles = {"ADMINISTRADOR"})
    void criarPacienteComoAdministrador() {
        when(pacienteService.save(any(PacienteRequest.class)))
                .thenReturn(Uni.createFrom().item(pacienteResponse));

        given()
            .contentType(ContentType.JSON)
            .body(pacienteRequest)
        .when()
            .post("/pacientes")
        .then()
            .statusCode(201);
    }

    @Test
    @DisplayName("Deve retornar erro de validação ao criar paciente sem nome")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void criarPacienteSemNome() {
        pacienteRequest.setNome(null);

        given()
            .contentType(ContentType.JSON)
            .body(pacienteRequest)
        .when()
            .post("/pacientes")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Deve retornar erro de validação ao criar paciente com CPF inválido")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void criarPacienteCpfInvalido() {
        pacienteRequest.setCpf("12345678901");

        given()
            .contentType(ContentType.JSON)
            .body(pacienteRequest)
        .when()
            .post("/pacientes")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Deve retornar erro de validação ao criar paciente com email inválido")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void criarPacienteEmailInvalido() {
        pacienteRequest.setEmail("email-invalido");

        given()
            .contentType(ContentType.JSON)
            .body(pacienteRequest)
        .when()
            .post("/pacientes")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Deve listar pacientes com página padrão quando não informada")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void listarPacientesPaginaPadrao() {
        PanachePage<PacienteResponse> panachePage = PanachePage.<PacienteResponse>builder()
                .content(List.of(pacienteResponse))
                .page(Page.of(0, 10))
                .totalCount(1L)
                .build();

        when(pacienteService.findPaginated(any(Page.class), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(panachePage));

        given()
        .when()
            .get("/pacientes")
        .then()
            .statusCode(200)
            .body("content", notNullValue());
    }
}


