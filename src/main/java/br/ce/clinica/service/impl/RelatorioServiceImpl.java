package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.RelatorioRequest;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.dto.response.RelatorioResponse;
import br.ce.clinica.dto.response.RelatorioResumeResponse;
import br.ce.clinica.entity.Relatorio;
import br.ce.clinica.repository.PacienteRepository;
import br.ce.clinica.repository.RelatorioRepository;
import br.ce.clinica.service.RelatorioService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.Set;

@ApplicationScoped
public class RelatorioServiceImpl implements RelatorioService {

    private static final Set<String> SORT_FIELDS_ALLOWED = Set.of(
            "id",
            "relatorio"
    );

    @Inject
    RelatorioRepository relatorioRepository;

    @Inject
    PacienteRepository pacienteRepository;

    @Override
    public Uni<RelatorioResponse> save(RelatorioRequest relatorioRequest) {
        return Panache.withTransaction(() -> pacienteRepository.find("id", relatorioRequest.getPacienteId())
                .firstResult()
                .onItem().ifNull().failWith(() -> new NotFoundException("Paciente não encontrado"))
                .onItem()
                .transformToUni( relatorioDoPaciente -> {
                    Relatorio relatorio = new Relatorio();
                    relatorio.setTexto(relatorioRequest.getTexto());
                    relatorio.setPaciente(relatorioDoPaciente);

                    return relatorioRepository.persist(relatorio);
                })
                .onItem().transform(RelatorioResponse::toResponse)
        );
    }

    @Override
    public Uni<RelatorioResumeResponse> findById(Long id) {
        return relatorioRepository.findById(id)
                .onItem().ifNull().failWith(() -> new NotFoundException("Relatorio não encontrado"))
                .onItem().transform(RelatorioResumeResponse:: toResponse);
    }

    @Override
    public Uni<Boolean> deleteById(Long id) {
        return Panache.withTransaction(() -> relatorioRepository.find("id", id)
                .firstResult()
                .onItem().ifNull().failWith(() -> new NotFoundException("Relatório do paciente não encontado"))
                .onItem().ifNotNull().transformToUni(relatorio -> relatorioRepository.deleteById(id)));

    }

    @Override
    public Uni<RelatorioResumeResponse> update(Long id, RelatorioRequest relatorioRequest) {
        return Panache.withTransaction(() ->
                relatorioRepository.findById(id)
                        .onItem().ifNull().failWith(
                                () -> new NotFoundException("Relatório do paciente não encontrado")
                        )
                        .onItem().invoke(relatorio -> {
                            relatorio.setTexto(relatorioRequest.getTexto());
                        })
                        .onItem().transform(RelatorioResumeResponse::toResponse)
        );
    }

    @Override
    public Uni<RelatorioResponse> findByIdWithPaciente(Long id) {
        return relatorioRepository.findByIdWithPaciente(id)
                .onItem().ifNull().failWith(() -> new NotFoundException("Relatório não encontrado"))
                .onItem().transform(RelatorioResponse::toResponse);
    }

    @Override
    public Uni<PanachePage<RelatorioResponse>> findPaginated(
            Page page,
            String sort,
            List<String> filterFields,
            List<String> filterValues) {

        Sort panacheSort = null;

        if (sort != null && !sort.isBlank()) {
            String[] split = sort.split(",");
            String field = split[0].trim();

            if (!SORT_FIELDS_ALLOWED.contains(field)) {
                throw new IllegalArgumentException(
                        "Campo de ordenação invalido: " + field
                );
            }

            boolean asc = split.length < 2 || split[1].equalsIgnoreCase("asc");

            panacheSort = asc
                    ? Sort.by(field).ascending()
                    : Sort.by(field).descending();
        }

        PanacheQuery<Relatorio> query =
                relatorioRepository.findPaginated(
                panacheSort,
                filterFields,
                filterValues
        );

        return Uni.combine().all().unis(
                query.page(page).list(),
                query.count()
        ).asTuple()
                .map(tuple -> PanachePage.<RelatorioResponse>builder()
                        .content(
                                tuple.getItem1()
                                        .stream()
                                        .map(RelatorioResponse::toResponse)
                                        .toList()
                        )
                        .page(page)
                        .totalCount(tuple.getItem2())
                        .build()
                );
    }
}




