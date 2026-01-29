package br.ce.clinica.service;

import br.ce.clinica.dto.request.FiliacaoRequest;
import br.ce.clinica.dto.response.FiliacaoResponse;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface FiliacaoService {

    Uni<List<FiliacaoResponse>> findByPacienteId(Long pacienteId);

    Uni<FiliacaoResponse> update(Long id, FiliacaoRequest filiacaoRequest);

    Uni<Void> deleteById(Long id);
}
