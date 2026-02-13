package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.CarteiraRequest;
import br.ce.clinica.dto.response.CarteiraResumeResponse;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.entity.Carteira;
import br.ce.clinica.exception.BadRequestBusinessException;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.exception.UnprocessableEntityBusinessException;
import br.ce.clinica.repository.PacienteRepository;
import br.ce.clinica.repository.CarteiraRepository;
import br.ce.clinica.service.CarteiraService;
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
public class CarteiraServiceImpl implements CarteiraService {

    @Inject
    CarteiraRepository carteiraRepository;

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
    public Uni<CarteiraResumeResponse> save(CarteiraRequest carteiraRequest) {
        return Panache.withTransaction(() -> pacienteRepository.find("id", carteiraRequest.getPacienteId())
                .firstResult()
                .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Paciente não encontrado"))
                .onItem().ifNotNull().invoke(paciente -> {
                    if(Boolean.FALSE.equals(paciente.getStatus())){
                        throw new UnprocessableEntityBusinessException("Paciente inativo, não é possível realizar transações");
                    }
                })
                .onItem().transformToUni(paciente -> {
                    Carteira carteira = new Carteira();
                    carteira.setDescricao(carteiraRequest.getDescricao());
                    carteira.setValor(carteiraRequest.getValor());
                    carteira.setTipoMovimento(carteiraRequest.getTipoMovimento());
                    carteira.setTipoDePagamento(carteiraRequest.getTipoDePagamento());
                    carteira.setPaciente(paciente);
                    return carteiraRepository.persist(carteira)
                            .onItem().transform(CarteiraResumeResponse::toResponse);
                })
        );
    }

    @Override
    public Uni<CarteiraResumeResponse> findById(Long id) {
        return carteiraRepository.findByIdWithPaciente(id)
                .onItem().ifNull().failWith(
                        () -> new NotFoundBusinessException("Transação não encontrada")
                )
                .onItem().transform(CarteiraResumeResponse::toResponse);
    }

//    @Override
//    public Uni<Boolean> deleteById(Long id) {
//        return Panache.withTransaction(() -> carteiraRepository.find("id", id)
//                .firstResult()
//                .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Transação não encontrada"))
//                .onItem().ifNotNull().transformToUni(transacao -> carteiraRepository.deleteById(id)));
//    }

    @Override
    public Uni<CarteiraResumeResponse> update(Long id, CarteiraRequest carteiraRequest) {
        return Panache.withTransaction(() ->
                carteiraRepository.find("id", id)
                        .firstResult()
                        .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Transação não encontrada"))
                        .chain(transacao ->
                                pacienteRepository.findById(carteiraRequest.getPacienteId())
                                        .onItem().ifNull().failWith(() -> new NotFoundBusinessException("Paciente não encontrado"))
                                        .onItem().ifNotNull().invoke(paciente -> {
                                            if (Boolean.FALSE.equals(paciente.getStatus())) {
                                                throw new UnprocessableEntityBusinessException("Paciente inativo, não é possível realizar transações");
                                            }
                                        })
                                        .replaceWith(transacao)
                        )
                        .onItem().invoke(transacao -> {
                            transacao.setDescricao(carteiraRequest.getDescricao());
                            transacao.setValor(carteiraRequest.getValor());
                            transacao.setTipoMovimento(carteiraRequest.getTipoMovimento());
                            transacao.setTipoDePagamento(carteiraRequest.getTipoDePagamento());
                        })
                        .onItem().transform(CarteiraResumeResponse::toResponse)
        );
    }


    @Override
    public Uni<PanachePage<CarteiraResumeResponse>> findPaginated(
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

            panacheSort = asc
                    ? Sort.by("t." + field).ascending()
                    : Sort.by("t." + field).descending();
        }

        PanacheQuery<Carteira> query =
                carteiraRepository.findPaginated(
                        panacheSort,
                        filterFields,
                        filterValues
                );

        return Uni.combine().all().unis(
                        query.page(page).list(),
                        query.count()
                ).asTuple()
                .map(tuple -> PanachePage.<CarteiraResumeResponse>builder()
                        .content(
                                tuple.getItem1()
                                        .stream()
                                        .map(CarteiraResumeResponse::toResponse)
                                        .toList()
                        )
                        .page(page)
                        .totalCount(tuple.getItem2())
                        .build()
                );
    }
}
