package br.ce.clinica.security;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

@RequestScoped
public class UsuarioLogadoProvider {

    private static final String USUARIO_SISTEMA = "system";

    @Inject
    JsonWebToken jwt;

    public Uni<String> getUsuarioLogado(){
        try {
            if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
                return Uni.createFrom().item(USUARIO_SISTEMA);
            }
            return Uni.createFrom().item(jwt.getSubject());
        } catch (Exception e) {
            return Uni.createFrom().item(USUARIO_SISTEMA);
        }
    }
}
