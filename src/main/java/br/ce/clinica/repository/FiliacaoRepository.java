package br.ce.clinica.repository;

import br.ce.clinica.entity.Filiacao;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class FiliacaoRepository implements PanacheRepository<Filiacao> {

    private static final String JPQL_WITH_FETCH = """
            SELECT DISTINCT f from Filiacao f
            LEFT JOIN FETCH f.paciente p
            WHERE p.id = ?1
            """;

    public Uni<List<Filiacao>> findByPacienteId(Long pacienteId) {
        return find(JPQL_WITH_FETCH, pacienteId).list();
    }
}
