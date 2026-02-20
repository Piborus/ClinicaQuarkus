package br.ce.clinica.service;

import br.ce.clinica.dto.request.AnamneseRequest;
import br.ce.clinica.dto.response.AnamneseResponse;
import br.ce.clinica.dto.response.PanachePage;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface AnamneseService {

    Uni<AnamneseResponse> save(AnamneseRequest anamneseRequest);

    Uni<AnamneseResponse> update(Long id, AnamneseRequest anamneseRequest);

    Uni<AnamneseResponse> findById(Long id);

//    Uni<AnamneseResponse> findByPacienteId(Long pacienteId);

//    Uni<Boolean> deleteById(Long id);

    Uni<PanachePage<AnamneseResponse>> findPaginated(
            Page page,
            String sort,
            List<String> filterFields,
            List<String> filterValues
    );
}
