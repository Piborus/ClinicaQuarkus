package br.ce.clinica.repository;

import br.ce.clinica.entity.Anamnese;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AnamneseRepository implements PanacheRepository<Anamnese> {

    private static final String JPQL_FIND_BY_ID = """
            SELECT DISTINCT a FROM Anamnese a
            LEFT JOIN FETCH a.paciente p
            LEFT JOIN FETCH p.responsaveis
            LEFT JOIN FETCH a.desenvolvimento d
            LEFT JOIN FETCH a.antecedenteFamiliar af
            WHERE p.id = ?1
            """;

    public Uni<Anamnese> findByIdWithCollections(Long pacienteId) {
        return find(JPQL_FIND_BY_ID, pacienteId).firstResult();
    }
}
