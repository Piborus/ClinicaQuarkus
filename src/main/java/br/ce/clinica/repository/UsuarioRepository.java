package br.ce.clinica.repository;

import br.ce.clinica.entity.Usuario;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepository<Usuario> {

    public Uni<Usuario> findByEmail(String email) {
        return find("email", email).firstResult();
    }
}
