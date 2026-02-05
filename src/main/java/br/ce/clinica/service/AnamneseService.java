package br.ce.clinica.service;

import br.ce.clinica.dto.request.AnamneseRequest;
import br.ce.clinica.dto.response.AnamneseResponse;
import io.smallrye.mutiny.Uni;

public interface AnamneseService {

    Uni<AnamneseResponse> save(AnamneseRequest anamneseRequest);

    Uni<AnamneseResponse> updade(Long id, AnamneseRequest anamneseRequest);

    Uni<AnamneseResponse> findById(Long id);

    Uni<Boolean> deleteById(Long id);
}
