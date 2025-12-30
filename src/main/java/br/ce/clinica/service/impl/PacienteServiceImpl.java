package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.PacienteRequest;
import br.ce.clinica.dto.response.PacienteResponse;
import br.ce.clinica.entity.Endereco;
import br.ce.clinica.entity.Paciente;
import br.ce.clinica.repository.PacienteRepository;
import br.ce.clinica.service.PacienteService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import java.time.OffsetDateTime;
import java.util.UUID;

@ApplicationScoped
public class PacienteServiceImpl implements PacienteService {

    @Inject
    PacienteRepository pacienteRepository;

    @Override
    public Uni<PacienteResponse> save(PacienteRequest pacienteRequest) {
        return Panache.withTransaction(() -> pacienteRepository.find("cpf", pacienteRequest.getCpf())
                .firstResult()
                .onItem().ifNotNull().failWith(() -> new RuntimeException("CPF ja existente!"))
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
                        endereco.setRua(pacienteRequest.getEndereco().getRua());
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
                .onItem().transform(PacienteResponse::fromEntity));
    }

    @Override
    public Uni<PacienteResponse> findById(Long id) {
        return pacienteRepository.find("id", id)
                .firstResult()
                .onItem().transform(PacienteResponse::fromEntity);
    }

    @Override
    public Uni<Boolean> deleteById(Long id) {
        return Panache.withTransaction(() -> pacienteRepository.find("id", id)
                .firstResult()
                .onItem().ifNull().failWith(() -> new RuntimeException("Paciente não encontrado"))
                .onItem().ifNotNull().transformToUni(paciente -> pacienteRepository.deleteById(id)));
    }

    @Override
    public Uni<PacienteResponse> update(Long id, PacienteRequest pacienteRequest) {
        return Panache.withTransaction(() -> pacienteRepository.find("id", id)
                .firstResult()
                .onItem().ifNull().failWith(() -> new NotFoundException("Paciente não encontrado"))
                .onItem().ifNotNull().transformToUni(
                        paciente -> {
                            paciente.setNome(pacienteRequest.getNome());
                            paciente.setIdade(pacienteRequest.getIdade());
                            paciente.setCpf(pacienteRequest.getCpf());
                            paciente.setSexo(pacienteRequest.getSexo());
                            paciente.setDataNascimento(pacienteRequest.getDataNascimento());
                            paciente.setCpf(pacienteRequest.getCpf());
                            paciente.setRg(paciente.getRg());
                            paciente.setTelefone(pacienteRequest.getTelefone());
                            paciente.setEmail(pacienteRequest.getEmail());
                            if (pacienteRequest.getEndereco() != null) {
                                Endereco endereco = paciente.getEndereco();
                                if (endereco == null) {
                                    endereco = new Endereco();
                                }
                                endereco.setRua(pacienteRequest.getEndereco().getRua());
                                endereco.setNumero(pacienteRequest.getEndereco().getNumero());
                                endereco.setBairro(pacienteRequest.getEndereco().getBairro());
                                endereco.setCep(pacienteRequest.getEndereco().getCep());
                                endereco.setComplemento(pacienteRequest.getEndereco().getComplemento());
                                endereco.setCidade(pacienteRequest.getEndereco().getCidade());
                                endereco.setEstado(pacienteRequest.getEndereco().getEstado());
                                endereco.setPais(pacienteRequest.getEndereco().getPais());
                                paciente.setEndereco(endereco);
                            }

                            return pacienteRepository.persist(paciente)
                                    .onItem().transform(PacienteResponse::fromEntity);
                        }
                )
        );
    }
}
