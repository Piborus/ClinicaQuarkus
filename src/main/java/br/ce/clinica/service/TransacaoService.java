package br.ce.clinica.service;

import br.ce.clinica.dto.request.TransacaoRequest;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.dto.response.TransacaoResponse;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface TransacaoService {

    Uni<TransacaoResponse> save(TransacaoRequest transacaoRequest);

    Uni<TransacaoResponse> findById(Long id);

    Uni<Boolean> deleteById(Long id);

    Uni<TransacaoResponse> update(Long id, TransacaoRequest transacaoRequest);

    Uni<PanachePage<TransacaoResponse>> findPaginated(
            Page page,
            String sort,
            List<String> filterFields,
            List<String> filterValues
    );

}
