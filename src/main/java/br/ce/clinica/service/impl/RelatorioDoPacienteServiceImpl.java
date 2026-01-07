package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.RelatorioDoPacienteRequest;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.dto.response.RelatorioDoPacienteResponse;
import br.ce.clinica.entity.RelatorioDoPaciente;
import br.ce.clinica.repository.PacienteRepository;
import br.ce.clinica.repository.RelatorioDoPacienteRepository;
import br.ce.clinica.service.RelatorioDoPacienteService;
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
public class RelatorioDoPacienteServiceImpl implements RelatorioDoPacienteService {

    private static final Set<String> SORT_FIELDS_ALLOWED = Set.of(
            "id",
            "relatorio"
    );

    @Inject
    RelatorioDoPacienteRepository relatorioDoPacienteRepository;

    @Inject
    PacienteRepository pacienteRepository;

    @Override
    public Uni<RelatorioDoPacienteResponse> save(RelatorioDoPacienteRequest relatorioDoPacienteRequest) {
        return Panache.withTransaction(() -> pacienteRepository.find("id", relatorioDoPacienteRequest.getPacienteId())
                .firstResult()
                .onItem().ifNull().failWith(() -> new NotFoundException("Paciente não encontrado"))
                .onItem()
                .transformToUni( relatorioDoPaciente -> {
                    RelatorioDoPaciente relatorio = new RelatorioDoPaciente();
                    relatorio.setRelatorio(relatorioDoPacienteRequest.getRelatorio());
                    relatorio.setPaciente(relatorioDoPaciente);

                    return relatorioDoPacienteRepository.persist(relatorio);
                })
                .onItem().transform(RelatorioDoPacienteResponse::toResponse)
        );
    }

    @Override
    public Uni<RelatorioDoPacienteResponse> findById(Long id) {
        return relatorioDoPacienteRepository.findById(id)
                .onItem().ifNull().failWith(() -> new NotFoundException("Relatorio não encontrado"))
                .onItem().transform(RelatorioDoPacienteResponse :: toResponse);
    }

    @Override
    public Uni<Boolean> deleteById(Long id) {
        return Panache.withTransaction(() -> relatorioDoPacienteRepository.deleteById(id))
                .onItem()
                .transform(delete -> {
                    if (delete) {
                        return true;
                    } else {
                        throw new NotFoundException("Relatório do paciente não encontrado");
                    }
                });
    }

    @Override
    public Uni<RelatorioDoPacienteResponse> update(Long id, RelatorioDoPacienteRequest relatorioDoPacienteRequest) {
        return Panache.withTransaction(() -> relatorioDoPacienteRepository.findById(id))
                .onItem().ifNull().failWith(() -> new NotFoundException("Relatório do paciente não encontrado"))
                .onItem().transformToUni(relatorioDoPaciente -> {
                    relatorioDoPaciente.setRelatorio(relatorioDoPacienteRequest.getRelatorio());
                    return relatorioDoPacienteRepository.persist(relatorioDoPaciente);
                })
                .onItem().transform(RelatorioDoPacienteResponse::toResponse);
    }

    @Override
    public Uni<RelatorioDoPacienteResponse> findByIdWithPaciente(Long id) {
        return relatorioDoPacienteRepository.findByIdWithPaciente(id)
                .onItem().ifNull().failWith(() -> new NotFoundException("Relatório não encontrado"))
                .onItem().transform(RelatorioDoPacienteResponse::toDetailedResponse);
    }

    @Override
    public Uni<PanachePage<RelatorioDoPacienteResponse>> findPaginated(
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

        PanacheQuery<RelatorioDoPaciente> query =
                relatorioDoPacienteRepository.findPaginated(
                panacheSort,
                filterFields,
                filterValues
        );

        return Uni.combine().all().unis(
                query.page(page).list(),
                query.count()
        ).asTuple()
                .map(tuple -> PanachePage.<RelatorioDoPacienteResponse>builder()
                        .content(
                                tuple.getItem1()
                                        .stream()
                                        .map(RelatorioDoPacienteResponse ::toResponse)
                                        .toList()
                        )
                        .page(page)
                        .totalCount(tuple.getItem2())
                        .build()
                );
    }
}




