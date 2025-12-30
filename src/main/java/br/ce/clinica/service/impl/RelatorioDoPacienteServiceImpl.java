package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.RelatorioDoPacienteRequest;
import br.ce.clinica.dto.response.RelatorioDoPacienteResponse;
import br.ce.clinica.entity.RelatorioDoPaciente;
import br.ce.clinica.repository.PacienteRepository;
import br.ce.clinica.repository.RelatorioDoPacienteRepository;
import br.ce.clinica.service.RelatorioDoPacienteService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class RelatorioDoPacienteServiceImpl implements RelatorioDoPacienteService {

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
                .onItem().transform(RelatorioDoPacienteResponse::fromEntity)
        );
    }

    @Override
    public Uni<RelatorioDoPacienteResponse> findById(Long id) {
        return relatorioDoPacienteRepository.findById(id)
                .onItem().ifNull().failWith(() -> new NotFoundException("Paciente não encontrado"))
                .onItem().transform(RelatorioDoPacienteResponse :: fromEntity);
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
                .onItem().transform(RelatorioDoPacienteResponse::fromEntity);
    }
}
