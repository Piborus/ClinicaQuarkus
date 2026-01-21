package br.ce.clinica.repository;


import br.ce.clinica.entity.Paciente;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PacienteRepository implements PanacheRepository<Paciente> {
}
