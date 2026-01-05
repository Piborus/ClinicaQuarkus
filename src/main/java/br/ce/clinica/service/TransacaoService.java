package br.ce.clinica.service;

import br.ce.clinica.dto.request.TransacaoRequest;
import br.ce.clinica.dto.response.TransacaoResponse;
import io.smallrye.mutiny.Uni;

public interface TransacaoService {

    Uni<TransacaoResponse> save(TransacaoRequest transacaoRequest);

    Uni<TransacaoResponse> findById(Long id);

    Uni<Boolean> deleteById(Long id);

    Uni<TransacaoResponse> update(Long id, TransacaoRequest transacaoRequest);
}
