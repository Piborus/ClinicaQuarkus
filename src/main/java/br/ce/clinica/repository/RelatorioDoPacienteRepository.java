package br.ce.clinica.repository;

import br.ce.clinica.entity.RelatorioDoPaciente;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RelatorioDoPacienteRepository implements PanacheRepository<RelatorioDoPaciente> {

    public Uni<RelatorioDoPaciente> findByIdWithPaciente(Long id) {
        return find("""
            SELECT r
            FROM RelatorioDoPaciente r
            JOIN FETCH r.paciente
            WHERE r.id = ?1
        """, id).firstResult();
    }

}
