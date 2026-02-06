package br.ce.clinica.resource;

import br.ce.clinica.dto.request.PacienteRequest;
import br.ce.clinica.enums.Sexo;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class PacienteResourceTest {

    @Test
    public void testSalvarPaciente() {
        PacienteRequest request = new PacienteRequest();
        request.setNome("João da Silva");
        request.setCpf("123.456.789-01");
        request.setRg("200312312334");
        request.setDataNascimento(LocalDate.of(1990, 1, 1));
        request.setSexo(Sexo.MASCULINO);
        request.setTelefone("(11) 91234-5678");
        request.setEmail("jj@gmail.com");
        request.setIdade(30);

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/pacientes")
            .then()
            .statusCode(201)
            .body("nome", is("João da Silva"))
            .body("dataNascimento", is("1990-01-01"))
            .body("id", notNullValue());
    }

    @Test
    public void testUpdatePacienteMesmoCpf() {
        PacienteRequest request = new PacienteRequest();
        request.setNome("Maria Oliveira");
        request.setCpf("123.456.789-02");
        request.setRg("12345678");
        request.setDataNascimento(LocalDate.of(1985, 5, 15));
        request.setSexo(Sexo.FEMININO);
        request.setIdade(38);

        Long id = given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/pacientes")
            .then()
            .statusCode(201)
            .extract().path("id");

        // Tentar atualizar com o MESMO CPF
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().put("/pacientes/{id}", id)
            .then()
            .statusCode(200)
            .body("cpf", is("123.456.789-02"));
    }

    @Test
    public void testUpdatePacienteComResponsavel() {
        br.ce.clinica.dto.request.FiliacaoRequest resp = new br.ce.clinica.dto.request.FiliacaoRequest();
        resp.setNome("Pai do Joao");
        resp.setCpf("000.000.000-00");
        resp.setGrauDeParentesco("PAI");

        PacienteRequest request = new PacienteRequest();
        request.setNome("Joaozinho");
        request.setCpf("123.456.789-10");
        request.setRg("10101010");
        request.setDataNascimento(LocalDate.of(2015, 1, 1));
        request.setSexo(Sexo.MASCULINO);
        request.setResponsaveis(java.util.List.of(resp));

        Long id = given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/pacientes")
            .then()
            .statusCode(201)
            .extract().path("id");

        // Atualizar o paciente mantendo o mesmo responsável
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when().put("/pacientes/{id}", id)
            .then()
            .statusCode(200)
            .body("responsaveis[0].cpf", is("000.000.000-00"));
    }
}
