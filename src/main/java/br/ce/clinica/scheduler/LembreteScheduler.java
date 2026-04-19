package br.ce.clinica.scheduler;

import br.ce.clinica.dto.request.LembreteDeConsultaRequest;
import br.ce.clinica.entity.Consulta;
import br.ce.clinica.repository.ConsultaRepository;
import br.ce.clinica.service.EmailService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class LembreteScheduler {

    private static final Logger log = Logger.getLogger(LembreteScheduler.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Inject
    ConsultaRepository consultaRepository;

    @Inject
    EmailService emailService;

    @Scheduled(cron = "0 0/3 * * * ?") //
    @WithSession
    public Uni<Void> enviarLembretes() {
        return enviarLembretes(LocalDateTime.now());
    }

    Uni<Void> enviarLembretes(LocalDateTime agora) {
        LocalDateTime inicio = agora.plusHours(3);
        LocalDateTime fim = inicio.plusMinutes(3);

        log.infof("Iniciando rotina de lembretes. Janela: %s até %s", inicio, fim);

        return consultaRepository.buscarConsultasParaLembrete(inicio, fim)
                .onItem().transformToUni(consultas -> {
                    log.infof("Consultas elegíveis encontradas para lembrete: %d", consultas.size());

                    List<Uni<Void>> envios = new ArrayList<>();

                    for (Consulta consulta : consultas) {
                        if (!consultaEhDoMesmoDia(agora, consulta)) {
                            log.debugf("Consulta %s ignorada: não pertence ao mesmo dia da execução", consulta.getId());
                            continue;
                        }

                        if (consulta.getPaciente() == null || consulta.getUsuario() == null) {
                            log.debugf("Consulta %s ignorada: paciente ou usuário ausente", consulta.getId());
                            continue;
                        }

                        if (consulta.getPaciente().getEmail() != null && !consulta.getPaciente().getEmail().isBlank()) {
                            log.infof("Preparando lembrete para paciente %s na consulta %s", consulta.getPaciente().getEmail(), consulta.getId());
                            envios.add(emailService.mandarLembreConsulta(
                                    buildRequest(consulta, consulta.getPaciente().getEmail())));
                        } else {
                            log.debugf("Consulta %s sem e-mail de paciente; lembrete não será enviado ao paciente", consulta.getId());
                        }

                        if (consulta.getUsuario().getEmail() != null && !consulta.getUsuario().getEmail().isBlank()) {
                            log.infof("Preparando lembrete para usuário %s na consulta %s", consulta.getUsuario().getEmail(), consulta.getId());
                            envios.add(emailService.mandarLembreConsulta(
                                    buildRequest(consulta, consulta.getUsuario().getEmail())));
                        } else {
                            log.debugf("Consulta %s sem e-mail de usuário; lembrete não será enviado ao usuário", consulta.getId());
                        }
                    }

                    if (envios.isEmpty()) {
                        log.info("Nenhum lembrete elegível para envio nesta execução.");
                        return Uni.createFrom().voidItem();
                    }

                    log.infof("Total de lembretes a enviar nesta execução: %d", envios.size());
                    return Uni.combine().all().unis(envios).usingConcurrencyOf(5).discardItems();
                });
    }

    private boolean consultaEhDoMesmoDia(LocalDateTime agora, Consulta consulta) {
        return consulta.getDataInicio() != null
                && consulta.getDataInicio().toLocalDate().isEqual(agora.toLocalDate());
    }

    private LembreteDeConsultaRequest buildRequest(Consulta c, String destinatario) {
        return LembreteDeConsultaRequest.builder()
                .destinatario(destinatario)
                .nomePaciente(c.getPaciente().getNome())
                .nomeProfissional(c.getUsuario().getNome())
                .especialidade(c.getUsuario().getTipoUsuario().name())
                .dataConsulta(c.getDataInicio().format(DATE_FORMATTER))
                .horaConsulta(c.getDataInicio().format(TIME_FORMATTER))
                .build();
    }
}
