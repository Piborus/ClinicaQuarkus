package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.ProntuarioRequest;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.dto.response.ProntuarioResponse;
import br.ce.clinica.dto.response.ProntuarioResumeResponse;
import br.ce.clinica.entity.Prontuario;
import br.ce.clinica.exception.BadRequestBusinessException;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.repository.PacienteRepository;
import br.ce.clinica.repository.ProntuarioRepository;
import br.ce.clinica.service.ProntuarioService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ProntuarioServiceImpl implements ProntuarioService {

    private static final Set<String> SORT_FIELDS_ALLOWED = Set.of(
            "id",
            "texto"
    );

    @Inject
    ProntuarioRepository prontuarioRepository;

    @Inject
    PacienteRepository pacienteRepository;

    @Override
    public Uni<ProntuarioResponse> save(ProntuarioRequest prontuarioRequest) {
        return Panache.withTransaction(() -> pacienteRepository.find("id", prontuarioRequest.getPacienteId())
                .firstResult()
                .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Paciente nao encontrado"))
                .onItem()
                .transformToUni(prontuarioDoPaciente -> {
                    Prontuario prontuario = new Prontuario();
                    prontuario.setTexto(prontuarioRequest.getTexto());
                    prontuario.setPaciente(prontuarioDoPaciente);

                    return prontuarioRepository.persist(prontuario);
                })
                .onItem().transform(ProntuarioResponse::toResponse)
        );
    }

    @Override
    public Uni<ProntuarioResumeResponse> findById(Long id) {
        return prontuarioRepository.findById(id)
                .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Prontuario nao encontrado"))
                .onItem().transform(ProntuarioResumeResponse::toResponse);
    }

    @Override
    public Uni<Boolean> deleteById(Long id) {
        return Panache.withTransaction(() -> prontuarioRepository.find("id", id)
                .firstResult()
                .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Prontuario do paciente nao encontrado"))
                .onItem().ifNotNull().transformToUni(prontuario -> prontuarioRepository.deleteById(id)));

    }

    @Override
    public Uni<ProntuarioResumeResponse> update(Long id, ProntuarioRequest prontuarioRequest) {
        return Panache.withTransaction(() -> pacienteRepository.find("id", prontuarioRequest.getPacienteId())
                .firstResult()
                        .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Paciente nao encontrado"))
                .onItem()
                        .transformToUni(prontuario -> prontuarioRepository.findById(id))
                        .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Prontuario do paciente nao encontrado")))
                .onItem()
                .invoke(prontuario -> {
                    prontuario.setTexto(prontuarioRequest.getTexto());
                })
                .onItem().transform(ProntuarioResumeResponse::toResponse);
    }

    @Override
    public Uni<ProntuarioResponse> findByIdWithPaciente(Long id) {
        return prontuarioRepository.findByIdWithPaciente(id)
                .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Prontuario nao encontrado"))
                .onItem().transform(ProntuarioResponse::toResponse);
    }

    @Override
    public Uni<PanachePage<ProntuarioResponse>> findPaginated(
            Page page,
            String sort,
            List<String> filterFields,
            List<String> filterValues) {

        Sort panacheSort = null;

        if (sort != null && !sort.isBlank()) {
            String[] split = sort.split(",");
            String field = split[0].trim();

            if (!SORT_FIELDS_ALLOWED.contains(field)) {
                throw new BadRequestBusinessException(
                        "Campo de ordenacao invalido: " + field
                );
            }

            boolean asc = split.length < 2 || split[1].equalsIgnoreCase("asc");

            panacheSort = asc
                    ? Sort.by("p." + field).ascending()
                    : Sort.by("p." + field).descending();
        }

        PanacheQuery<Prontuario> query =
                prontuarioRepository.findPaginated(
                panacheSort,
                filterFields,
                filterValues
        );

        return Uni.combine().all().unis(
                query.page(page).list(),
                query.count()
        ).asTuple()
                .map(tuple -> PanachePage.<ProntuarioResponse>builder()
                        .content(
                                tuple.getItem1()
                                        .stream()
                                        .map(ProntuarioResponse::toResponse)
                                        .toList()
                        )
                        .page(page)
                        .totalCount(tuple.getItem2())
                        .build()
                );
    }
}
