package br.ce.clinica.service;

import br.ce.clinica.dto.request.PacienteRequest;
import br.ce.clinica.dto.response.PacienteResponse;
import io.smallrye.mutiny.Uni;

public interface PacienteService {

     Uni<PacienteResponse> save(PacienteRequest pacienteRequest);

     Uni<PacienteResponse> findById(Long id);

     Uni<Boolean> deleteById(Long id);

     Uni<PacienteResponse> update(Long id, PacienteRequest pacienteRequest);

}
