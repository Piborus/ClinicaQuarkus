package br.ce.clinica.service;

import br.ce.clinica.dto.request.PacienteRequest;
import br.ce.clinica.dto.response.PacienteResponse;
import br.ce.clinica.dto.response.PacienteResumeResponse;
import br.ce.clinica.dto.response.PanachePage;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface PacienteService {

     Uni<PacienteResponse> save(PacienteRequest pacienteRequest);

     Uni<PacienteResponse> findById(Long id);

     Uni<Boolean> deleteById(Long id);

     Uni<PacienteResponse> update(Long id, PacienteRequest pacienteRequest);

     Uni<PanachePage<PacienteResumeResponse>> findPaginated(
             Page page,
             String sort,
             List<String> filterFields,
             List<String> filterValues
     );

}
