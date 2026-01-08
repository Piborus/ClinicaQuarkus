package br.ce.clinica.repository;

import br.ce.clinica.entity.Transacao;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransacaoRepository implements PanacheRepository<Transacao> {

    public Uni<Transacao> findByIdWithPaciente(Long id) {
        return find("""
                SELECT t
                FROM Transacao t
                JOIN FETCH t.paciente
                WHERE t.id = :id
                """, id).firstResult();
    }
}
