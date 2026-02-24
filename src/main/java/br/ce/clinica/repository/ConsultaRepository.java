package br.ce.clinica.repository;

import br.ce.clinica.entity.Consulta;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConsultaRepository implements PanacheRepository<Consulta> {
}
