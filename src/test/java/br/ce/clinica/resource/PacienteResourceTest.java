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
            .statusCode(200)
            .body("nome", is("João da Silva"))
            .body("dataNascimento", is("1990-01-01"))
            .body("id", notNullValue());
    }
}
