package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.LembreteDeConsultaRequest;
import br.ce.clinica.service.EmailService;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.Location;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class EmailServiceImpl implements EmailService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Inject
    @Location("mail/lembreteConsulta")
    MailTemplate template;

    @Override
    public Uni<String> enviarLembreConsulta(
            String destinatario,
            String nomePaciente,
            String nomeProfissional,
            String dataConsulta,
            String horaConsulta) {
        return template.to(destinatario)
                .subject("Lembrete de Consulta")
                .data("nomePaciente", nomePaciente)
                .data("nomeProfissional", nomeProfissional)
                .data("dataConsulta", dataConsulta)
                .data("horaConsulta", horaConsulta)
                .send()
                .map(v -> "Lembrete enviado com sucesso para " + destinatario);
    }

    @Override
    public Uni<Void> mandarLembreConsulta(LembreteDeConsultaRequest request) {
        return template.to(request.getDestinatario())
                .subject("Lembrete de Consulta")
                .data("nomePaciente", request.getNomePaciente())
                .data("nomeProfissional", request.getNomeProfissional())
                .data("especialidade", request.getEspecialidade())
                .data("dataConsulta", request.getDataConsulta())
                .data("horaConsulta", request.getHoraConsulta())
                .send();
    }
}
