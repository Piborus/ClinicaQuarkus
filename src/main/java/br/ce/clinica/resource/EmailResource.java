package br.ce.clinica.resource;

import br.ce.clinica.dto.response.ApiResponse;
import br.ce.clinica.openapi.ApiDocumentation;
import br.ce.clinica.scheduler.LembreteScheduler;
import br.ce.clinica.service.EmailService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/mailer")
@Produces(MediaType.TEXT_PLAIN)
@ApplicationScoped
@ApiDocumentation
public class EmailResource {

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

//    @POST
//    @Path("/lembrete/enviar")
//    @WithSession
//    public Uni<RestResponse<String>> enviarLembretes() {
//        return lembreteScheduler.enviarLembretes()
//                .map(v -> RestResponse.ok("Lembretes enviados com sucesso"));
//    }

    @POST
    @Path("/lembrete/consultas/disparar")
    @WithSession
    @RolesAllowed({"ADMINISTRADOR", "PSICOLOGO"})
    @Operation(summary = "Dispara lembretes de consultas", description = "Executa manualmente a rotina de envio de lembretes de consulta 3 horas antes do horário agendado")
    public Uni<RestResponse<Void>> dispararLembretesConsulta() {
        return lembreteScheduler.enviarLembretes()
                .replaceWith(RestResponse.ok());
    }

    @POST
    @Path("/esqueci-senha/{email}")
    @WithSession
    @Operation(summary = "Envia um email para recuperar a senha", description = "Envia um email para recuperar a senha")
    public Uni<RestResponse<ApiResponse>> enviarEmailEsqueciSenha(
            @PathParam("email") String email
    ) {
        return emailService.esqueciSenha(email)
                .onItem()
                .transform(esqueciSenha -> RestResponse.status(RestResponse.Status.CREATED,
                        ApiResponse.builder()
                                .message("Email de recuperação de senha enviado com sucesso")
                                .build()
                        ));
    }
}
