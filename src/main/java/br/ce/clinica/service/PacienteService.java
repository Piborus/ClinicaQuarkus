package br.ce.clinica.service;

import br.ce.clinica.dto.request.PacienteRequest;
import br.ce.clinica.dto.response.PacienteResponse;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.dto.response.RelatorioDoPacienteResponse;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.UUID;

public interface PacienteService {

    public Uni<PacienteResponse> save(PacienteRequest pacienteRequest);

    public Uni<PacienteResponse> findById(Long id);

    public Uni<Boolean> deleteById(Long id);

    public Uni<PacienteResponse> update(Long id, PacienteRequest pacienteRequest);

}
