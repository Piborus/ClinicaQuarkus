package br.ce.clinica.repository;

import br.ce.clinica.entity.AntecedenteFamiliar;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AntecedenteFamiliarRepository implements PanacheRepository<AntecedenteFamiliar> {
}
