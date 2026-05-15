package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.PacienteRequest;
import br.ce.clinica.dto.response.PacienteResponse;
import br.ce.clinica.dto.response.PacienteResumeResponse;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.entity.Endereco;
import br.ce.clinica.entity.Filiacao;
import br.ce.clinica.entity.Paciente;
import br.ce.clinica.exception.BadRequestBusinessException;
import br.ce.clinica.exception.ConflictBusinessException;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.exception.UnprocessableEntityBusinessException;
import br.ce.clinica.repository.*;
import br.ce.clinica.service.PacienteService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@ApplicationScoped
public class PacienteServiceImpl implements PacienteService {

    @Inject
    PacienteRepository pacienteRepository;

    @Inject
    CarteiraRepository carteiraRepository;

    @Inject
    ProntuarioRepository prontuarioRepository;

    @Inject
    FiliacaoRepository filiacaoRepository;

    @Inject
    AnamneseRepository anamneseRepository;

    @Inject
    UsuarioRepository usuarioRepository;

    private static final List<String> SORT_FIELDS_ALLOWED = List.of(
            "id",
            "nome",
            "cpf",
            "rg",
            "dataNascimento",
            "sexo",
            "telefone",
            "email",
            "idade"
    );

    @Override
    public Uni<PacienteResponse> save(PacienteRequest pacienteRequest) {
        return Panache.withTransaction(() ->
                pacienteRepository.find("cpf", pacienteRequest.getCpf())
                        .firstResult()
                        .onItem().ifNotNull().failWith(() -> new ConflictBusinessException("CPF ja existente!"))
                        .onItem().ifNull().switchTo(
                                pacienteRepository.find("rg", pacienteRequest.getRg())
                                        .firstResult()
                                        .onItem().ifNotNull().failWith(() -> new ConflictBusinessException("RG ja existente!"))
                        )
                        .onItem().ifNull().continueWith(() -> {
                            Paciente paciente = new Paciente();
                            paciente.setNome(pacienteRequest.getNome());
                            paciente.setCpf(pacienteRequest.getCpf());
                            paciente.setRg(pacienteRequest.getRg());
                            paciente.setDataNascimento(pacienteRequest.getDataNascimento());
                            paciente.setSexo(pacienteRequest.getSexo());
                            paciente.setTelefone(pacienteRequest.getTelefone());
                            paciente.setEmail(pacienteRequest.getEmail());
                            paciente.setIdade(pacienteRequest.getIdade());
                            paciente.setReligiao(pacienteRequest.getReligiao());
                            paciente.setNaturalidade(pacienteRequest.getNaturalidade());
                            paciente.setEscolaridade(pacienteRequest.getEscolaridade());
                            paciente.setProfissao(pacienteRequest.getProfissao());
                            paciente.setEstadoCivil(pacienteRequest.getEstadoCivil());

                            if (pacienteRequest.getEndereco() != null) {
                                Endereco endereco = new Endereco();
                                endereco.setLogradouro(pacienteRequest.getEndereco().getLogradouro());
                                endereco.setNumero(pacienteRequest.getEndereco().getNumero());
                                endereco.setBairro(pacienteRequest.getEndereco().getBairro());
                                endereco.setCep(pacienteRequest.getEndereco().getCep());
                                endereco.setComplemento(pacienteRequest.getEndereco().getComplemento());
                                endereco.setCidade(pacienteRequest.getEndereco().getCidade());
                                endereco.setEstado(pacienteRequest.getEndereco().getEstado());
                                endereco.setPais(pacienteRequest.getEndereco().getPais());
                                paciente.setEndereco(endereco);
                            }

                            return paciente;
                        })
                        .onItem().transformToUni(paciente -> usuarioRepository.find("id", pacienteRequest.getUsuarioId())
                                .firstResult()
                                .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Usuário não encontrado!"))
                                .invoke(paciente::setUsuario)
                                .replaceWith(paciente)
                        )
                        .onItem().transformToUni(paciente -> pacienteRepository.persist(paciente))
                        .chain(paciente -> updateFiliacao(paciente, pacienteRequest))
//                        .chain(paciente -> pacienteRepository.findByIdWithCollections(paciente.getId()))
                        .onItem().transform(PacienteResponse::toResponse)
        );
    }

    @Override
    public Uni<PacienteResponse> findById(Long id) {
        return pacienteRepository.findByIdWithCollections(id)
                .onItem().ifNull().failWith(() ->  new NotFoundBusinessException("Paciente não encontrado!"))
                .onItem().transform(PacienteResponse::toResponse);
    }

    public Uni<Boolean> softDelete(Long id) {
        return Panache.withTransaction(() ->
                pacienteRepository.findById(id)
                        .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Paciente não encontrado"))
                        .onItem().ifNotNull().invoke(paciente -> {
                            if (Boolean.FALSE.equals(paciente.getStatus())) {
                                throw new UnprocessableEntityBusinessException("Paciente já arquivado");
                            }
                        })
                        .invoke(paciente -> {
                            paciente.setStatus(false);
                            paciente.setDeletado(true);
                            paciente.setDataDelecao(OffsetDateTime.now());
                        })
                        .chain(() -> softDeleteRelacionados(id))
                        .replaceWith(Boolean.TRUE)
        );
    }

    @Override
    public Uni<Boolean> restore(Long id) {
        return Panache.withTransaction(() -> pacienteRepository.findById(id)
                .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Paciente não encontrado"))
                .onItem().ifNotNull().invoke(paciente -> {
                    if (Boolean.TRUE.equals(paciente.getStatus())){
                        throw new ConflictBusinessException("Paciente já ativo");
                    }
                })
                .invoke(paciente -> {
                    paciente.setStatus(true);
                    paciente.setDeletado(false);
                    paciente.setDataDelecao(null);
                })
                .replaceWith(Boolean.TRUE)
        );
    }

    @Override
    public Uni<PacienteResumeResponse> update(Long id, PacienteRequest pacienteRequest) {
        return Panache.withTransaction(() ->
                pacienteRepository.findById(id)
                        .onItem().ifNull().failWith(() ->
                                new NotFoundBusinessException("Paciente não encontrado")
                        )
                        .onItem().ifNotNull().invoke(paciente -> {
                            if (Boolean.FALSE.equals(paciente.getStatus())) {
                                throw new UnprocessableEntityBusinessException("Paciente arquivado, não é possível atualizar!");
                            }
                        })
                        .onItem().transformToUni(paciente ->
                                pacienteRepository.find(
                                                "cpf = ?1 and id <> ?2",
                                                pacienteRequest.getCpf(),
                                                id
                                        )
                                        .firstResult()
                                        .onItem().ifNotNull().failWith(() ->
                                                new ConflictBusinessException("CPF já existente!")
                                        )
                                        .replaceWith(paciente)
                        ).onItem().transformToUni(paciente ->
                                pacienteRepository.find(
                                                "rg = ?1 and id <> ?2",
                                                pacienteRequest.getRg(),
                                                id
                                        )
                                        .firstResult()
                                        .onItem().ifNotNull()
                                        .failWith(() -> new ConflictBusinessException("RG já existente!"))
                                        .replaceWith(paciente)
                        )
                        .onItem().transform(paciente -> {
                            paciente.setNome(pacienteRequest.getNome());
                            paciente.setIdade(pacienteRequest.getIdade());
                            paciente.setCpf(pacienteRequest.getCpf());
                            paciente.setSexo(pacienteRequest.getSexo());
                            paciente.setDataNascimento(pacienteRequest.getDataNascimento());
                            paciente.setRg(pacienteRequest.getRg());
                            paciente.setTelefone(pacienteRequest.getTelefone());
                            paciente.setEmail(pacienteRequest.getEmail());

                            if (pacienteRequest.getEndereco() != null) {
                                Endereco endereco = paciente.getEndereco();
                                if (endereco == null) {
                                    endereco = new Endereco();
                                }
                                endereco.setLogradouro(pacienteRequest.getEndereco().getLogradouro());
                                endereco.setNumero(pacienteRequest.getEndereco().getNumero());
                                endereco.setBairro(pacienteRequest.getEndereco().getBairro());
                                endereco.setCep(pacienteRequest.getEndereco().getCep());
                                endereco.setComplemento(pacienteRequest.getEndereco().getComplemento());
                                endereco.setCidade(pacienteRequest.getEndereco().getCidade());
                                endereco.setEstado(pacienteRequest.getEndereco().getEstado());
                                endereco.setPais(pacienteRequest.getEndereco().getPais());
                                paciente.setEndereco(endereco);
                            }

                            return paciente;
                        })
                        .onItem().transformToUni(paciente -> pacienteRepository.findByIdWithCollections(paciente.getId()))
                        .chain(paciente -> updateFiliacao(paciente, pacienteRequest))
                        .onItem().transform(PacienteResumeResponse::toResponse)
        );
    }


    @Override
    public Uni<PanachePage<PacienteResponse>> findPaginated(
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
                        "Campo de ordenação invalido: " + field
                );
            }

            boolean asc = split.length < 2 || split[1].equalsIgnoreCase("asc");
            panacheSort = asc ? Sort.by("p." + field).ascending() : Sort.by("p." + field).descending();
        }
        PanacheQuery<Paciente> query =
                pacienteRepository.findPaginated(
                        panacheSort,
                        filterFields,
                        filterValues
                );
        return Uni.combine().all().unis(
                        query.page(page).list(),
                        query.count()
                ).asTuple()
                .map(tuple ->
                        PanachePage.<PacienteResponse>builder()
                                .content(
                                        tuple.getItem1()
                                                .stream()
                                                .map(PacienteResponse::toResponse)
                                                .toList()
                                )
                                .page(page)
                                .totalCount(tuple.getItem2())
                                .build()
                );
    }

    private Uni<Paciente> updateFiliacao(Paciente paciente, PacienteRequest pacienteRequest) {
        if (pacienteRequest.getResponsaveis() == null || pacienteRequest.getResponsaveis().isEmpty()) {
            return Uni.createFrom().item(paciente);
        }

        if (paciente.getResponsaveis() == null) {
            paciente.setResponsaveis(new HashSet<>());
        }

        return Multi.createFrom().iterable(pacienteRequest.getResponsaveis())
                .onItem().transformToUniAndConcatenate(filiacaoRequest ->
                        filiacaoRepository.find("cpf", filiacaoRequest.getCpf())
                                .firstResult()
                                .onItem().transformToUni(existing -> {
                                    if (existing != null) {
                                        boolean alreadyLinkedToPaciente =
                                                paciente.getResponsaveis().stream()
                                                        .anyMatch(r -> Objects.equals(r.getId(), existing.getId()));

                                        if (!alreadyLinkedToPaciente) {
                                            return Uni.createFrom().failure(new ConflictBusinessException("CPF já existente!"));
                                        }

                                        existing.setNome(filiacaoRequest.getNome());
                                        existing.setIdade(filiacaoRequest.getIdade());
                                        existing.setEmail(filiacaoRequest.getEmail());
                                        existing.setTelefone(filiacaoRequest.getTelefone());
                                        existing.setGrauDeParentesco(filiacaoRequest.getGrauDeParentesco());
                                        existing.setPaciente(paciente);

                                        return filiacaoRepository.persist(existing)
                                                .invoke(persisted -> paciente.getResponsaveis().add(persisted))
                                                .replaceWith(existing);
                                    }

                                    Filiacao filiacao = new Filiacao();
                                    filiacao.setNome(filiacaoRequest.getNome());
                                    filiacao.setIdade(filiacaoRequest.getIdade());
                                    filiacao.setCpf(filiacaoRequest.getCpf());
                                    filiacao.setEmail(filiacaoRequest.getEmail());
                                    filiacao.setTelefone(filiacaoRequest.getTelefone());
                                    filiacao.setGrauDeParentesco(filiacaoRequest.getGrauDeParentesco());
                                    filiacao.setPaciente(paciente);

                                    return filiacaoRepository.persist(filiacao)
                                            .invoke(persisted -> paciente.getResponsaveis().add(persisted))
                                            .replaceWith(filiacao);
                                })
                )
                .collect().asList()
                .replaceWith(paciente);
    }


    private Uni<Void> softDeleteRelacionados(Long pacienteId) {
        OffsetDateTime now = OffsetDateTime.now();
        return Uni.combine().all().unis(
                filiacaoRepository.update(
                        "status = false, deletado = true, dataDelecao = ?1 where paciente.id = ?2",
                        now, pacienteId),
                carteiraRepository.update(
                        "status = false, deletado = true, dataDelecao = ?1 where paciente.id = ?2",
                        now, pacienteId),
                prontuarioRepository.update(
                        "status = false, deletado = true, dataDelecao = ?1 where paciente.id = ?2",
                        now, pacienteId),
                anamneseRepository.update(
                        "status = false, deletado = true, dataDelecao = ?1 where paciente.id = ?2",
                        now, pacienteId)

        ).asTuple().replaceWithVoid();
    }

}

