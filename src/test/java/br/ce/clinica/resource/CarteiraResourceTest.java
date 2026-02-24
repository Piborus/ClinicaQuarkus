package br.ce.clinica.resource;

import br.ce.clinica.dto.request.CarteiraRequest;
import br.ce.clinica.dto.response.CarteiraResumeResponse;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.enums.TipoDePagamento;
import br.ce.clinica.enums.TipoMovimento;
import br.ce.clinica.exception.ConflictBusinessException;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.service.CarteiraService;
import io.quarkus.panache.common.Page;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@QuarkusTest
@DisplayName("CarteiraResource Integration Tests")
class CarteiraResourceTest {

    @InjectMock
    CarteiraService carteiraService;

    private CarteiraRequest carteiraRequest;
    private CarteiraResumeResponse carteiraResumeResponse;

    @BeforeEach
    void setUp() {
        carteiraRequest = CarteiraRequest.builder()
                .valor(new BigDecimal("150.00"))
                .descricao("Pagamento consulta")
                .tipoDePagamento(TipoDePagamento.PIX)
                .tipoMovimento(TipoMovimento.ENTRADA)
                .pacienteId(1L)
                .build();

        carteiraResumeResponse = CarteiraResumeResponse.builder()
                .id(1L)
                .valor(new BigDecimal("150.00"))
                .descricao("Pagamento consulta")
                .tipoDePagamento(TipoDePagamento.PIX)
                .tipoMovimento(TipoMovimento.ENTRADA)
                .build();
    }

    @Test
    @DisplayName("Deve criar transação com sucesso")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void criarTransacaoComSucesso() {
        when(carteiraService.save(any(CarteiraRequest.class)))
                .thenReturn(Uni.createFrom().item(carteiraResumeResponse));

        given()
            .contentType(ContentType.JSON)
            .body(carteiraRequest)
        .when()
            .post("/carteira")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("valor", notNullValue());
    }

    @Test
    @DisplayName("Deve retornar erro ao criar transação para paciente inexistente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void criarTransacaoPacienteNaoEncontrado() {
        when(carteiraService.save(any(CarteiraRequest.class)))
                .thenReturn(Uni.createFrom().failure(new NotFoundBusinessException("Paciente nao encontrado")));

        given()
            .contentType(ContentType.JSON)
            .body(carteiraRequest)
        .when()
            .post("/carteira")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Deve retornar erro ao criar transação para paciente inativo")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void criarTransacaoPacienteInativo() {
        when(carteiraService.save(any(CarteiraRequest.class)))
                .thenReturn(Uni.createFrom().failure(new ConflictBusinessException("Paciente inativo")));

        given()
            .contentType(ContentType.JSON)
            .body(carteiraRequest)
        .when()
            .post("/carteira")
        .then()
            .statusCode(409);
    }

    @Test
    @DisplayName("Deve buscar transação por ID com sucesso")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void buscarTransacaoPorIdComSucesso() {
        when(carteiraService.findById(1L))
                .thenReturn(Uni.createFrom().item(carteiraResumeResponse));

        given()
        .when()
            .get("/carteira/1")
        .then()
            .statusCode(200)
            .body("id", is(1))
            .body("valor", notNullValue());
    }

    @Test
    @DisplayName("Deve retornar erro ao buscar transação inexistente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void buscarTransacaoNaoEncontrada() {
        when(carteiraService.findById(999L))
                .thenReturn(Uni.createFrom().failure(new NotFoundBusinessException("Transacao nao encontrada")));

        given()
        .when()
            .get("/carteira/999")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Deve atualizar transação com sucesso")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void atualizarTransacaoComSucesso() {
        when(carteiraService.update(eq(1L), any(CarteiraRequest.class)))
                .thenReturn(Uni.createFrom().item(carteiraResumeResponse));

        given()
            .contentType(ContentType.JSON)
            .body(carteiraRequest)
        .when()
            .put("/carteira/1")
        .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("Deve retornar erro ao atualizar transação inexistente")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void atualizarTransacaoNaoEncontrada() {
        when(carteiraService.update(eq(999L), any(CarteiraRequest.class)))
                .thenReturn(Uni.createFrom().failure(new NotFoundBusinessException("Transacao nao encontrada")));

        given()
            .contentType(ContentType.JSON)
            .body(carteiraRequest)
        .when()
            .put("/carteira/999")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Deve listar transações paginadas")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void listarTransacoesPaginadas() {
        PanachePage<CarteiraResumeResponse> panachePage = PanachePage.<CarteiraResumeResponse>builder()
                .content(List.of(carteiraResumeResponse))
                .page(Page.of(0, 10))
                .totalCount(1L)
                .build();

        when(carteiraService.findPaginated(any(Page.class), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(panachePage));

        given()
            .queryParam("page", 1)
            .queryParam("size", 10)
        .when()
            .get("/carteira")
        .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("totalCount", is(1));
    }

    @Test
    @DisplayName("Deve listar transações com filtros")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void listarTransacoesComFiltros() {
        PanachePage<CarteiraResumeResponse> panachePage = PanachePage.<CarteiraResumeResponse>builder()
                .content(List.of(carteiraResumeResponse))
                .page(Page.of(0, 10))
                .totalCount(1L)
                .build();

        when(carteiraService.findPaginated(any(Page.class), any(), any(), any()))
                .thenReturn(Uni.createFrom().item(panachePage));

        given()
            .queryParam("page", 1)
            .queryParam("size", 10)
            .queryParam("sort", "valor,desc")
            .queryParam("filterFields", "tipoPagamento")
            .queryParam("filterValues", "PIX")
        .when()
            .get("/carteira")
        .then()
            .statusCode(200)
            .body("content", notNullValue());
    }


    @Test
    @DisplayName("Deve retornar erro de validação ao criar transação sem valor")
    @TestSecurity(user = "test", roles = {"PSICOLOGO"})
    void criarTransacaoSemValor() {
        carteiraRequest.setValor(null);

        given()
            .contentType(ContentType.JSON)
            .body(carteiraRequest)
        .when()
            .post("/carteira")
        .then()
            .statusCode(400);
    }

}


