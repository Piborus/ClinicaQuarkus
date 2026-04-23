package br.ce.clinica.resource;

import br.ce.clinica.scheduler.LembreteScheduler;
import br.ce.clinica.service.EmailService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

@QuarkusTest
@DisplayName("MailerResource Integration Tests")
class MailerResourceTest {

    @InjectMock
    EmailService emailService;

    @InjectMock
    LembreteScheduler lembreteScheduler;

    @Test
    @DisplayName("Deve enviar email manual com sucesso")
    void sendEmailComSucesso() {
        when(emailService.enviarLembreConsulta(
                "cayawod291@fpxnet.com",
                "Haroldo",
                "Dr. JoÃ£o Silva",
                "25/12/2024",
                "14:30"
        )).thenReturn(Uni.createFrom().item("Lembrete enviado com sucesso"));

        given()
        .when()
                .get("/mailer/send")
        .then()
                .statusCode(200)
                .body(is("Email enviado com sucesso"));
    }

    @Test
    @DisplayName("Deve disparar rotina de lembretes com perfil autorizado")
    @TestSecurity(user = "psicologo", roles = {"PSICOLOGO"})
    void dispararLembretesComSucesso() {
        when(lembreteScheduler.enviarLembretes())
                .thenReturn(Uni.createFrom().voidItem());

        given()
        .when()
                .post("/mailer/lembrete/consultas/disparar")
        .then()
                .statusCode(200)
                .body(is("Rotina de lembretes de consulta executada com sucesso"));
    }

    @Test
    @DisplayName("Deve negar disparo de lembretes sem perfil autorizado")
    @TestSecurity(user = "recepcao", roles = {"RECEPCIONISTA"})
    void dispararLembretesSemPermissao() {
        given()
        .when()
                .post("/mailer/lembrete/consultas/disparar")
        .then()
                .statusCode(403);
    }
}
