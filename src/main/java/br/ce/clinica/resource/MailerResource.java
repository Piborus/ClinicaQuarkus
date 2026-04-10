package br.ce.clinica.resource;

import br.ce.clinica.openapi.ApiDocumentation;
import br.ce.clinica.scheduler.LembreteScheduler;
import br.ce.clinica.service.EmailService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/mailer")
@Produces(MediaType.TEXT_PLAIN)
@ApplicationScoped
@ApiDocumentation
public class MailerResource {

    @Inject
    EmailService emailService;

    @Inject
    LembreteScheduler lembreteScheduler;

    @GET
    @Path("/send")
    public Uni<String> sendEmail() {
        return emailService.enviarLembreConsulta(
                "cayawod291@fpxnet.com",
                "Haroldo",
                "Dr. João Silva",
                "25/12/2024",
                "14:30"
        ).replaceWith("Email enviado com sucesso");
    }

    @POST
    @Path("/lembrete/enviar")
    @WithSession
    public Uni<RestResponse<String>> enviarLembretes() {
        return lembreteScheduler.enviarLembretes()
                .map(v -> RestResponse.ok("Lembretes enviados com sucesso"));
    }

    @POST
    @Path("/lembrete/consultas/disparar")
    @WithSession
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    @Operation(summary = "Dispara lembretes de consultas", description = "Executa manualmente a rotina de envio de lembretes de consulta 3 horas antes do horário agendado")
    public Uni<RestResponse<String>> dispararLembretesConsulta() {
        return lembreteScheduler.enviarLembretes()
                .replaceWith(RestResponse.ok("Rotina de lembretes de consulta executada com sucesso"));
    }
}
