package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.PacienteRequest;
import br.ce.clinica.dto.response.PacienteResponse;
import br.ce.clinica.dto.response.PacienteResumeResponse;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.entity.Endereco;
import br.ce.clinica.entity.Paciente;
import br.ce.clinica.exception.BusinessException;
import br.ce.clinica.repository.PacienteRepository;
import br.ce.clinica.repository.TransacaoRepository;
import br.ce.clinica.service.PacienteService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import java.util.List;


@ApplicationScoped
public class PacienteServiceImpl implements PacienteService {

    @Inject
    PacienteRepository pacienteRepository;

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

    @Inject
    TransacaoRepository transacaoRepository;

    @Override
    public Uni<PacienteResponse> save(PacienteRequest pacienteRequest) {
        return Panache.withTransaction(() -> pacienteRepository.find("cpf", pacienteRequest.getCpf())
                .firstResult()
                .onItem().ifNotNull().failWith(() -> new BusinessException("CPF ja existente!"))
                .onItem().ifNull().switchTo(
                        pacienteRepository.find("rg", pacienteRequest.getRg()).firstResult()
                                .onItem().ifNotNull().failWith(() -> new BusinessException("RG ja existente!"))
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
                .onItem().transformToUni(paciente -> pacienteRepository.persist(paciente))
                .onItem().transform(PacienteResponse::toResponse));
    }

    @Override
    public Uni<PacienteResponse> findById(Long id) {
        return pacienteRepository.find("id", id)
                .firstResult()
                .onItem().ifNull().failWith(() ->  new NotFoundException("Paciente não encontrado!"))
                .onItem().transform(PacienteResponse::toResponse);
    }

    @Override
    public Uni<Boolean> deleteById(Long id) {
        return Panache.withTransaction(() -> pacienteRepository.find("id", id)
                .firstResult()
                .onItem().ifNull().failWith(() -> new NotFoundException("Paciente não encontrado"))
                .onItem().ifNotNull().transformToUni(paciente -> pacienteRepository.deleteById(id)));
    }

    @Override
    public Uni<PacienteResponse> update(Long id, PacienteRequest pacienteRequest) {
        return Panache.withTransaction(() ->
                pacienteRepository.findById(id)
                        .onItem().ifNull().failWith(() ->
                                new NotFoundException("Paciente não encontrado")
                        )
                        .onItem().transformToUni(paciente ->
                                pacienteRepository.find(
                                                "cpf = ?1 and id <> ?2",
                                                pacienteRequest.getCpf(),
                                                id
                                        )
                                        .firstResult()
                                        .onItem().ifNotNull().failWith(() ->
                                                new BusinessException("CPF já existente!")
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
                                        .failWith(() -> new BusinessException("RG já existente!"))
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
                        .onItem().transform(PacienteResponse::toResponse)
        );
    }

    @Override
    public Uni<PanachePage<PacienteResumeResponse>> findPaginated(
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

            panacheSort = asc ? Sort.by(field).ascending() : Sort.by(field).descending();

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
        ).asTuple().map(tuple -> PanachePage.<PacienteResumeResponse>builder()
                .content(
                        tuple.getItem1()
                                .stream()
                                .map(PacienteResumeResponse::toResponse)
                                .toList()
                )
                .page(page)
                .totalCount(tuple.getItem2())
                .build()
        );
    }
}
