package br.ce.clinica.service;

import br.ce.clinica.dto.request.AgendaRequest;
import br.ce.clinica.dto.response.ConsultaResponse;
import io.smallrye.mutiny.Uni;

import java.time.LocalDate;
import java.util.List;

public interface AgendaService {

    Uni<ConsultaResponse> scheduleConsultation(AgendaRequest request);

    Uni<Void> cancelConsultation(
            Long id
//            AgendaCancelamentoRequest request
    );

    Uni<List<String>> findAvailableTimes(Long usuarioId, LocalDate data);

}
