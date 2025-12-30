package br.ce.clinica.repository;

import br.ce.clinica.entity.RelatorioDoPaciente;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RelatorioDoPacienteRepository implements PanacheRepository<RelatorioDoPaciente> {
}
