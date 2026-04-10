package br.ce.clinica.resource;

import br.ce.clinica.openapi.ApiDocumentation;
import br.ce.clinica.service.EmailService;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/mailer")
@Produces(MediaType.TEXT_PLAIN)
@ApplicationScoped
@ApiDocumentation
public class MailerResource {

    @Inject
    EmailService emailService;

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
}
