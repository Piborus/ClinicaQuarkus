package br.ce.clinica.service;

import br.ce.clinica.dto.request.RelatorioRequest;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.dto.response.RelatorioResponse;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface RelatorioService {

     Uni<RelatorioResponse> save(RelatorioRequest relatorioRequest);

     Uni<RelatorioResponse> findById(Long id);

     Uni<Boolean> deleteById(Long id);

     Uni<RelatorioResponse> update(Long id, RelatorioRequest relatorioRequest);

     Uni<RelatorioResponse> findByIdWithPaciente(Long id);

     Uni<PanachePage<RelatorioResponse>> findPaginated(
            Page page,
            String sort,
            List<String> filterFields,
            List<String> filterValues
    );
}
