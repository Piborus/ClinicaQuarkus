package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.AgendaRequest;
import br.ce.clinica.dto.response.ConsultaResponse;
import br.ce.clinica.entity.Consulta;
import br.ce.clinica.enums.StatusConfirmacao;
import br.ce.clinica.enums.StatusConsulta;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.repository.ConsultaRepository;
import br.ce.clinica.repository.PacienteRepository;
import br.ce.clinica.repository.UsuarioRepository;
import br.ce.clinica.service.AgendaService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Request;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@ApplicationScoped
public class AgendaServiceImpl implements AgendaService {
   //Cria uma exception
    @Inject
    ConsultaRepository consultaRepository;

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    PacienteRepository pacienteRepository;
    @Inject
    Request request;

    @Override
    public Uni<ConsultaResponse> scheduleConsultation(AgendaRequest request) {
        log.info("Iniciando agendamento de consulta para o paciente {} com o usuário {} no horário {}", 
                request.getIdpaciente(), request.getIdUsuario(), request.getHorario());
        
        return Panache.withTransaction(() ->
                pacienteRepository.findByIdWithCollections(request.getIdpaciente())
                        .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Paciente não encontrado."))
                        .onItem().ifNotNull().transformToUni(paciente -> usuarioRepository.findById(request.getIdUsuario())
                                .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Usuário não encontrado."))
                                .onItem().ifNotNull().transformToUni(usuario -> {
                                    Consulta consulta = new Consulta();
                                    consulta.setPaciente(paciente);
                                    consulta.setUsuario(usuario);
                                    consulta.setDataInicio(request.getHorario());
                                    consulta.setDataFim(request.getHorario().plusHours(1));
                                    consulta.setStatusConsulta(StatusConsulta.AGENDADA);
                                    consulta.setStatusConfirmacao(StatusConfirmacao.PENDENTE);
                                    return consultaRepository.persist(consulta);
                                })
                                .onItem().invoke(consulta -> log.info("Consulta agendada com sucesso. ID: {}", consulta.getId()))
                                .onItem().transform(ConsultaResponse::toResponse)
        ));
    }

    @Override
    public Uni<Void> cancelConsultation(Long id) {
        log.info("Solicitação de cancelamento para a consulta ID: {}", id);
        
        return Panache.withTransaction(() -> consultaRepository.findById(id)
                .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Consulta não encontrada."))
                .onItem().ifNotNull().invoke(consulta -> {
                    consulta.setStatusConfirmacao(StatusConfirmacao.RECUSADA);
                    consulta.setStatusConsulta(StatusConsulta.CANCELADA);
//                    consulta.setObservacao(request.getObservacao());
                    log.info("Consulta ID: {} cancelada com sucesso.", id);
                }).replaceWithVoid());
    }

    @Override
    public Uni<List<String>> findAvailableTimes(Long usuarioId, LocalDate data) {
        LocalDateTime inicioDia = data.atTime(8, 0);
        LocalDateTime fimDia = data.atTime(18, 0); // limite comercial explícito

        return consultaRepository.buscarHorariosOcupadosDoDia(usuarioId, inicioDia, fimDia)
                .onItem().transform(ocupados -> {
                    List<String> disponiveis = new ArrayList<>();
                    LocalDateTime slot = inicioDia;
                    int i = 0;

                    while (slot.isBefore(fimDia)) {
                        LocalDateTime fimSlot = slot.plusHours(1);

                        while (i < ocupados.size() && !ocupados.get(i).getDataFim().isAfter(slot)) {
                            i++;
                        }

                        boolean conflito = i < ocupados.size()
                                && ocupados.get(i).getDataInicio().isBefore(fimSlot)
                                && ocupados.get(i).getDataFim().isAfter(slot);

                        if (!conflito) {
                            disponiveis.add(slot.toLocalTime().toString()); // HH:mm
                        }

                        slot = slot.plusHours(1);
                    }

                    return disponiveis;
                });
    }
}
