package br.ce.clinica.repository;

import br.ce.clinica.dto.request.IntervaloConsultaRequest;
import br.ce.clinica.entity.Consulta;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ConsultaRepository implements PanacheRepository<Consulta> {

    private static final String JPQL_BASE = """
            SELECT DISTINCT c FROM Consulta c
            LEFT JOIN FETCH c.paciente
            LEFT JOIN FETCH c.usuario
            WHERE 1 = 1
            """;


    private static final String BUSCAR_CONSULTA_DO_DIA = """
               SELECT c.dataInicio, c.dataFim
               FROM Consulta c
               WHERE c.usuario.id = ?1
               AND c.statusConsulta <> br.ce.clinica.enums.StatusConsulta.CANCELADA
               AND c.dataInicio < ?3
               AND c.dataFim > ?2
               ORDER BY c.dataInicio
            """;

    private static final String EXISTE_CONFLITO_HORARIO = """
            SELECT COUNT(c) > 0
            FROM Consulta c
            WHERE c.usuario.id = ?1
            AND c.statusConsulta <> br.ce.clinica.enums.StatusConsulta.CANCELADA
            AND c.dataInicio < ?3
            AND c.dataFim > ?2
            """;

    public Uni<List<IntervaloConsultaRequest>> buscarHorariosOcupadosDoDia(
            Long idUsuario,
            LocalDateTime dataInicio,
            LocalDateTime dataFim) {
        return find(BUSCAR_CONSULTA_DO_DIA, idUsuario, dataInicio, dataFim)
                .project(IntervaloConsultaRequest.class)
                .list();
    }

    public Uni<Boolean> existeConflitoHorario(
            Long idUsuario,
            LocalDateTime dataInicio,
            LocalDateTime dataFim) {
        return find(EXISTE_CONFLITO_HORARIO, idUsuario, dataInicio, dataFim)
                .project(Boolean.class)
                .firstResult()
                .onItem().ifNull().continueWith(false);

    }

}
