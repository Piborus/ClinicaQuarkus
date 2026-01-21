package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.TransacaoRequest;
import br.ce.clinica.dto.response.TransacaoResponse;
import br.ce.clinica.entity.Transacao;
import br.ce.clinica.repository.PacienteRepository;
import br.ce.clinica.repository.TransacaoRepository;
import br.ce.clinica.service.TransacaoService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class TransacaoServiceImpl implements TransacaoService {

    @Inject
    TransacaoRepository transacaoRepository;

    @Inject
    PacienteRepository pacienteRepository;

    @Override
    public Uni<TransacaoResponse> save(TransacaoRequest transacaoRequest) {
        return Panache.withTransaction(() -> pacienteRepository.find("id", transacaoRequest.getPacienteId())
                .firstResult()
                .onItem().ifNull().failWith(() -> new NotFoundException("Paciente não encontrado"))
                .onItem().transformToUni(paciente -> {
                    Transacao transacao = new Transacao();
                    transacao.setDescricao(transacaoRequest.getDescricao());
                    transacao.setCaixaEntrada(transacaoRequest.getCaixaEntrada());
                    transacao.setCaixaSaida(transacaoRequest.getCaixaSaida());
                    transacao.setTipoDePagamento(transacaoRequest.getTipoDePagamento());
                    transacao.setPaciente(paciente);
                    return transacaoRepository.persist(transacao)
                            .onItem().transform(TransacaoResponse::fromEntity);
                })
        );
    }

    @Override
    public Uni<TransacaoResponse> findById(Long id) {
        return transacaoRepository.findById(id)
                .onItem().ifNull().failWith(
                        () -> new NotFoundException("Transação não encontrada")
                )
                .onItem().transform(TransacaoResponse::fromEntity);
    }

    @Override
    public Uni<Boolean> deleteById(Long id) {
        return Panache.withTransaction(() -> transacaoRepository.deleteById(id))
                .onItem()
                .transform(delete -> {
                    if (delete) {
                        return true;
                    } else {
                        throw new NotFoundException("Transação não encontrada");
                    }
                });
    }

    @Override
    public Uni<TransacaoResponse> update(Long id, TransacaoRequest transacaoRequest) {
        return Panache.withTransaction(() -> transacaoRepository.find("id", id)
                .firstResult()
                .onItem().ifNull().failWith(() -> new NotFoundException("Transação não encontrada"))
                .onItem().transformToUni(transacao -> {
                    transacao.setDescricao(transacaoRequest.getDescricao());
                    transacao.setCaixaEntrada(transacaoRequest.getCaixaEntrada());
                    transacao.setCaixaSaida(transacaoRequest.getCaixaSaida());
                    transacao.setTipoDePagamento(transacaoRequest.getTipoDePagamento());
                    return transacaoRepository.persist(transacao);
                })
                .onItem().transform(TransacaoResponse::fromEntity)
        );
    }
}
