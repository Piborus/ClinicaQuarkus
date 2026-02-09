package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.AnamneseRequest;
import br.ce.clinica.dto.response.AnamneseResponse;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.entity.Anamnese;
import br.ce.clinica.entity.AnamneseDesenvolvimento;
import br.ce.clinica.entity.AntecedenteFamiliar;
import br.ce.clinica.enums.TipoAnamnese;
import br.ce.clinica.exception.BadRequestBusinessException;
import br.ce.clinica.exception.ConflictBusinessException;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.repository.AnamneseDesenvolvimentoRepository;
import br.ce.clinica.repository.AnamneseRepository;
import br.ce.clinica.repository.AntecedenteFamiliarRepository;
import br.ce.clinica.repository.PacienteRepository;
import br.ce.clinica.service.AnamneseService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Slf4j
@ApplicationScoped
public class AnamneseServiceImpl implements AnamneseService {

    @Inject
    AnamneseRepository anamneseRepository;

    @Inject
    PacienteRepository pacienteRepository;

    @Inject
    AnamneseDesenvolvimentoRepository desenvolvimentoRepository;

    @Inject
    AntecedenteFamiliarRepository familiarRepository;

    private static final List<String> SORT_FIELDS_ALLOWED = List.of(
            "id",
            "tipoAnamnese",
            "encaminhamento",
            "historicoAcompanhamento",
            "psicodinamicaFamiliar",
            "observacao",
            "dataCriacao",
            "dataAtualizacao"
    );


    @Override
    public Uni<AnamneseResponse> save(AnamneseRequest request) {
        return Panache.withTransaction(() ->
                pacienteRepository.findByIdWithCollections(request.getPacienteId())
                        .onItem().ifNull()
                        .failWith(() -> new NotFoundBusinessException("Paciente não encontrado."))
                        .chain(paciente ->
                                anamneseRepository.find("paciente.id", paciente.getId())
                                        .firstResult()
                                        .onItem().ifNotNull()
                                        .failWith(() ->
                                                new ConflictBusinessException("Paciente já possui anamnese cadastrada.")
                                        )
                                        .replaceWith(paciente)
                        )
                        .chain(paciente -> {
                            Anamnese anamnese = new Anamnese();
                            anamnese.setPaciente(paciente);
                            anamnese.setTipoAnamnese(TipoAnamnese.INICIAL);
                            anamnese.setEncaminhamento(request.getEncaminhamento());
                            anamnese.setHistoricoAcompanhamento(request.getHistoricoAcompanhamento());
                            anamnese.setPsicodinamicaFamiliar(request.getPsicodinamicaFamiliar());
                            anamnese.setObservacao(request.getObservacao());
                            return anamneseRepository.persist(anamnese);
                        })
                        .chain(anamnese -> updateDesenvolvimento(anamnese, request))
                        .chain(anamnese -> updateAntecedenteFamiliar(anamnese, request))
                        .onItem().transform(AnamneseResponse::toResponse)
        );
    }

    @Override
    public Uni<AnamneseResponse> updade(Long id, AnamneseRequest anamneseRequest) {
        return Panache.withTransaction(() -> anamneseRepository.find("id", id)
                .firstResult()
                .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Anamnese não encontrada."))
                .onItem().invoke(anamnese -> {
                    anamnese.setTipoAnamnese(TipoAnamnese.REAVALIACAO);
                    anamnese.setEncaminhamento(anamneseRequest.getEncaminhamento());
                    anamnese.setHistoricoAcompanhamento(anamneseRequest.getHistoricoAcompanhamento());
                    anamnese.setPsicodinamicaFamiliar(anamneseRequest.getPsicodinamicaFamiliar());
                    anamnese.setObservacao(anamneseRequest.getObservacao());
                })
                .chain(anamnese -> updateDesenvolvimento(anamnese, anamneseRequest))
                .chain(anamnese ->  updateAntecedenteFamiliar(anamnese, anamneseRequest))
                .onItem().transform(AnamneseResponse::toResponse)
        );
    }

    @Override
    public Uni<AnamneseResponse> findById(Long id) {
        return anamneseRepository.findByIdWithCollections(id)
                    .onItem().ifNull().failWith(
                            () -> new NotFoundBusinessException("Anamnese não encontrada.")
                    )
                    .onItem().transform(AnamneseResponse::toResponse);

    }

    @Override
    public Uni<AnamneseResponse> findByPacienteId(Long pacienteId) {

        return anamneseRepository.findByPacienteIdWithCollections(pacienteId)
                .onItem().ifNull().failWith(
                        () -> new NotFoundBusinessException("Anamnese não encontrada para o paciente informado.")
                )
                .onItem().transform(AnamneseResponse::toResponse);
    }

    @Override
    public Uni<Boolean> deleteById(Long id) {
        return Panache.withTransaction(
                () -> anamneseRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Anamnese não encontrada."))
                        .onItem().ifNotNull().transformToUni(anamnese -> anamneseRepository.deleteById(id))
                        .replaceWith(true)
        );
    }

    @Override
    public Uni<PanachePage<AnamneseResponse>> findPaginated(
            Page page,
            String sort,
            List<String> filterFields,
            List<String> filterValues
    ) {
        Sort panacheSort = null;

        if (sort != null && !sort.isBlank()) {
            String[] split = sort.split(",");
            String field = split[0].trim();

            if (!SORT_FIELDS_ALLOWED.contains(field)) {
                throw new BadRequestBusinessException(
                        "Campo de ordenação invalido: " + field
                );
            }

            boolean asc = split.length < 2 || split[1].equalsIgnoreCase("asc");
            panacheSort = asc
                    ? Sort.by("a." + field).ascending()
                    : Sort.by("a." + field).descending();
        }
        PanacheQuery<Anamnese> query =
                anamneseRepository.findPaginated(
                        panacheSort,
                        filterFields,
                        filterValues
                );
        return Uni.combine().all().unis(
                        query.page(page).list(),
                        query.count()
                ).asTuple()
                .map(tuple ->
                        PanachePage.<AnamneseResponse>builder()
                                .content(
                                        tuple.getItem1()
                                                .stream()
                                                .map(AnamneseResponse::toResponse)
                                                .toList()
                                )
                                .page(page)
                                .totalCount(tuple.getItem2())
                                .build()
                );
    }

    private Uni<Anamnese> updateDesenvolvimento(
            Anamnese anamnese,
            AnamneseRequest request
    ) {
        if (request.getDesenvolvimento() == null) {
            return Uni.createFrom().item(anamnese);
        }

        AnamneseDesenvolvimento desenvolvimento = anamnese.getDesenvolvimento();

        if (desenvolvimento == null) {
            desenvolvimento = new AnamneseDesenvolvimento();
            desenvolvimento.setAnamnese(anamnese);
            anamnese.setDesenvolvimento(desenvolvimento);
        }

        var dto = request.getDesenvolvimento();

        desenvolvimento.setGravidezParto(dto.getGravidezParto());
        desenvolvimento.setMemoriasInfancia(dto.getMemoriasInfancia());
        desenvolvimento.setMemoriasAdolescencia(dto.getMemoriasAdolescencia());
        desenvolvimento.setFaseAdulta(dto.getFaseAdulta());
        desenvolvimento.setFaseAtual(dto.getFaseAtual());
        desenvolvimento.setMoraComQuem(dto.getMoraComQuem());
        desenvolvimento.setNumeroFilhos(dto.getNumeroFilhos());
        desenvolvimento.setNumeroIrmaos(dto.getNumeroIrmaos());
        desenvolvimento.setOrdemNascimento(dto.getOrdemNascimento());
        desenvolvimento.setFumante(dto.getFumante());
        desenvolvimento.setEtilista(dto.getEtilista());
        desenvolvimento.setUsoMedicamento(dto.getUsoMedicamento());
        desenvolvimento.setDescricaoMedicamentos(dto.getDescricaoMedicamentos());
        desenvolvimento.setRotina(dto.getRotina());

        return Uni.createFrom().item(anamnese);
    }


    private Uni<Anamnese> updateAntecedenteFamiliar(
            Anamnese anamnese,
            AnamneseRequest request
    ) {
        if (request.getAntecedenteFamiliar() == null) {
            return Uni.createFrom().item(anamnese);
        }

        AntecedenteFamiliar antecedenteFamiliar = anamnese.getAntecedenteFamiliar();

        if (antecedenteFamiliar == null) {
            antecedenteFamiliar = new AntecedenteFamiliar();
            antecedenteFamiliar.setAnamnese(anamnese);
            anamnese.setAntecedenteFamiliar(antecedenteFamiliar);
        }

        var dto = request.getAntecedenteFamiliar();

        antecedenteFamiliar.setAnamnese(anamnese);
        antecedenteFamiliar.setTiposAntecedentes(
                dto.getTiposAntecedentes() == null
                        ? Collections.emptySet()
                        : new HashSet<>(dto.getTiposAntecedentes())
        );
        antecedenteFamiliar.setDescricao(dto.getDescricao());
        return Uni.createFrom().item(anamnese);
    }
}
