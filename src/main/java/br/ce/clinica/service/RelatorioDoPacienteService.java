package br.ce.clinica.service;

import br.ce.clinica.dto.request.RelatorioDoPacienteRequest;
import br.ce.clinica.dto.response.RelatorioDoPacienteResponse;
import io.smallrye.mutiny.Uni;

public interface RelatorioDoPacienteService {

    public Uni<RelatorioDoPacienteResponse> save(RelatorioDoPacienteRequest relatorioDoPacienteRequest);

    public Uni<RelatorioDoPacienteResponse> findById(Long id);

    public Uni<Boolean> deleteById(Long id);

    public Uni<RelatorioDoPacienteResponse> update(Long id, RelatorioDoPacienteRequest relatorioDoPacienteRequest);
}
