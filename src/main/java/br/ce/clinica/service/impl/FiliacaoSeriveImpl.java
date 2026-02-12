package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.FiliacaoRequest;
import br.ce.clinica.dto.response.FiliacaoResponse;
import br.ce.clinica.exception.ConflictBusinessException;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.repository.FiliacaoRepository;
import br.ce.clinica.repository.PacienteRepository;
import br.ce.clinica.service.FiliacaoService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class FiliacaoSeriveImpl implements FiliacaoService {

    @Inject
    FiliacaoRepository filiacaoRepository;

    @Inject
    PacienteRepository pacienteRepository;

    @Override
    public Uni<List<FiliacaoResponse>> findByPacienteId(Long pacienteId) {
        return pacienteRepository.find("id", pacienteId)
                .firstResult()
                .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Paciente não encontrado."))
                .onItem().ifNotNull().invoke( paciente -> {
                    if (Boolean.FALSE.equals(paciente.getStatus())) {
                            throw new ConflictBusinessException("Paciente inativo, não é possível consultar as filiações.");
                    }
                })
                .onItem().ifNotNull().transformToUni(paciente -> filiacaoRepository.findByPacienteId(pacienteId)
                        .onItem().ifNull().failWith(
                                () -> new NotFoundBusinessException("Filiacões não encontradas para o paciente informado."))
                        .onItem()
                        .transform(
                                filiacaos -> filiacaos
                                        .stream()
                                        .map(FiliacaoResponse::toResponse).toList()));
    }

    @Override
    public Uni<FiliacaoResponse> update(Long id, FiliacaoRequest filiacaoRequest) {
        return Panache.withTransaction(() -> filiacaoRepository.find("id", id)
                        .firstResult()
                        .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Filiação não encontrada."))
                        .onItem().ifNotNull().invoke( filiacao -> {
                            if (Boolean.FALSE.equals(filiacao.getPaciente().getStatus())) {
                                throw new ConflictBusinessException("Paciente inativo, não é possível atualizar as filiações.");
                            }
                        })
                        .onItem().invoke(filiacao -> {
                            filiacao.setNome(filiacaoRequest.getNome());
                            filiacao.setIdade(filiacaoRequest.getIdade());
                            filiacao.setCpf(filiacaoRequest.getCpf());
                            filiacao.setEmail(filiacaoRequest.getEmail());
                            filiacao.setTelefone(filiacaoRequest.getTelefone());
                            filiacao.setGrauDeParentesco(filiacaoRequest.getGrauDeParentesco());
                        })
                        .onItem().transform(FiliacaoResponse::toResponse)
                );
    }

//    @Override
//    public Uni<Void> deleteById(Long id) {
//        return Panache.withTransaction(() -> filiacaoRepository.find("id", id)
//                .firstResult()
//                .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Filiacão não encontrada.")))
//                .onItem().transformToUni(filiacao -> filiacaoRepository.delete(filiacao)
//        );
//    }


}
