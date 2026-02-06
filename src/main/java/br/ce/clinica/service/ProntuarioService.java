package br.ce.clinica.service;

import br.ce.clinica.dto.request.ProntuarioRequest;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.dto.response.ProntuarioResponse;
import br.ce.clinica.dto.response.ProntuarioResumeResponse;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface ProntuarioService {

     Uni<ProntuarioResponse> save(ProntuarioRequest prontuarioRequest);

     Uni<ProntuarioResumeResponse> findById(Long id);

     Uni<Boolean> deleteById(Long id);

     Uni<ProntuarioResumeResponse> update(Long id, ProntuarioRequest prontuarioRequest);

     //Uni<ProntuarioResponse> findById(Long id);

     Uni<PanachePage<ProntuarioResponse>> findPaginated(
            Page page,
            String sort,
            List<String> filterFields,
            List<String> filterValues
    );
}
