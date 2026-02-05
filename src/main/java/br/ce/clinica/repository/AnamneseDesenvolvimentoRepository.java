package br.ce.clinica.repository;

import br.ce.clinica.entity.AnamneseDesenvolvimento;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AnamneseDesenvolvimentoRepository implements PanacheRepository<AnamneseDesenvolvimento> {
}
