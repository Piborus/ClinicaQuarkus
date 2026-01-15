package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.TransacaoRequest;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.dto.response.RelatorioResponse;
import br.ce.clinica.dto.response.TransacaoResponse;
import br.ce.clinica.entity.Relatorio;
import br.ce.clinica.entity.Transacao;
import br.ce.clinica.repository.PacienteRepository;
import br.ce.clinica.repository.TransacaoRepository;
import br.ce.clinica.service.TransacaoService;
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
public class TransacaoServiceImpl implements TransacaoService {

    @Inject
    TransacaoRepository transacaoRepository;

    @Inject
    PacienteRepository pacienteRepository;

    private static final Set<String> SORT_FIELDS_ALLOWED = Set.of(
            "id",
            "valor",
            "descricao",
            "tipoMovimento",
            "tipoDePagamento"
    );

    @Override
    public Uni<TransacaoResponse> save(TransacaoRequest transacaoRequest) {
        return Panache.withTransaction(() -> pacienteRepository.find("id", transacaoRequest.getPacienteId())
                .firstResult()
                .onItem().ifNull().failWith(() -> new NotFoundException("Paciente não encontrado"))
                .onItem().transformToUni(paciente -> {
                    Transacao transacao = new Transacao();
                    transacao.setDescricao(transacaoRequest.getDescricao());
                    transacao.setValor(transacaoRequest.getValor());
                    transacao.setTipoMovimento(transacaoRequest.getTipoMovimento());
                    transacao.setTipoDePagamento(transacaoRequest.getTipoDePagamento());
                    transacao.setPaciente(paciente);
                    return transacaoRepository.persist(transacao)
                            .onItem().transform(TransacaoResponse::toResponse);
                })
        );
    }

    @Override
    public Uni<TransacaoResponse> findById(Long id) {
        return transacaoRepository.findByIdWithPaciente(id)
                .onItem().ifNull().failWith(
                        () -> new NotFoundException("Transação não encontrada")
                )
                .onItem().transform(TransacaoResponse::toResponse);
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
                    transacao.setValor(transacaoRequest.getValor());
                    transacao.setTipoMovimento(transacaoRequest.getTipoMovimento());
                    transacao.setTipoDePagamento(transacaoRequest.getTipoDePagamento());
                    return transacaoRepository.persist(transacao);
                })
                .onItem().transform(TransacaoResponse::toResponse)
        );
    }

    @Override
    public Uni<PanachePage<TransacaoResponse>> findPaginated(
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

        PanacheQuery<Transacao> query =
                transacaoRepository.findPaginated(
                        panacheSort,
                        filterFields,
                        filterValues
                );

        return Uni.combine().all().unis(
                        query.page(page).list(),
                        query.count()
                ).asTuple()
                .map(tuple -> PanachePage.<TransacaoResponse>builder()
                        .content(
                                tuple.getItem1()
                                        .stream()
                                        .map(TransacaoResponse::toResponse)
                                        .toList()
                        )
                        .page(page)
                        .totalCount(tuple.getItem2())
                        .build()
                );
    }
}
