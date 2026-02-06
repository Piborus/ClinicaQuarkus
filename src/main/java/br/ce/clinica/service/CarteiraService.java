package br.ce.clinica.service;

import br.ce.clinica.dto.request.CarteiraRequest;
import br.ce.clinica.dto.response.CarteiraResponse;
import br.ce.clinica.dto.response.CarteiraResumeResponse;
import br.ce.clinica.dto.response.PanachePage;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface CarteiraService {

    Uni<CarteiraResponse> save(CarteiraRequest carteiraRequest);

    Uni<CarteiraResumeResponse> findById(Long id);

    Uni<Boolean> deleteById(Long id);

    Uni<CarteiraResponse> update(Long id, CarteiraRequest carteiraRequest);

    Uni<PanachePage<CarteiraResponse>> findPaginated(
            Page page,
            String sort,
            List<String> filterFields,
            List<String> filterValues
    );

}
