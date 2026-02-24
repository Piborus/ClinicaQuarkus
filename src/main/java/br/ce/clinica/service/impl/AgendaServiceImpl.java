package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.AgendaRequest;
import br.ce.clinica.dto.response.ConsultaResponse;
import br.ce.clinica.repository.ConsultaRepository;
import br.ce.clinica.service.AgendaService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class AgendaServiceImpl implements AgendaService {

    @Inject
    ConsultaRepository consultaRepository;

    @Override
    public Uni<ConsultaResponse> scheduleConsultation(AgendaRequest request) {
        return null;
    }

    @Override
    public Uni<Void> cancelConsultation(Long consultaId) {
        return null;
    }

    @Override
    public Uni<List<LocalDateTime>> findAvailableTimes(Long usuarioId, LocalDate data) {
        return null;
    }
}
