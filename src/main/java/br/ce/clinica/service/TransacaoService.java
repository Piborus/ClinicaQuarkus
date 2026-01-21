package br.ce.clinica.service;

import br.ce.clinica.dto.request.TransacaoRequest;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.dto.response.TransacaoResumeResponse;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface TransacaoService {

    Uni<TransacaoResumeResponse> save(TransacaoRequest transacaoRequest);

    Uni<TransacaoResumeResponse> findById(Long id);

    Uni<Boolean> deleteById(Long id);

    Uni<TransacaoResumeResponse> update(Long id, TransacaoRequest transacaoRequest);

    Uni<PanachePage<TransacaoResumeResponse>> findPaginated(
            Page page,
            String sort,
            List<String> filterFields,
            List<String> filterValues
    );

}
