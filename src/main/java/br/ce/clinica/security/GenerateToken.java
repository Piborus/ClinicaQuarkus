package br.ce.clinica.security;

import br.ce.clinica.entity.Usuario;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class GenerateToken {

    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "clinica-api")
    String issuer;

    @ConfigProperty(name = "jwt.expiration.hours", defaultValue = "4")
    Long expirationHours;


    public String generateToken(Usuario usuario) {

        long expiredAt = Duration.ofHours(expirationHours).getSeconds();

        return Jwt.issuer(issuer)
                .subject(usuario.getId().toString())
                .upn(usuario.getEmail())
                .groups(Set.of(usuario.getTipoUsuario().name()))
                .claim("email", usuario.getEmail())
                .claim("tipoUsuario", usuario.getTipoUsuario().name())
                .expiresIn(expiredAt)
                .sign();
    }
}
