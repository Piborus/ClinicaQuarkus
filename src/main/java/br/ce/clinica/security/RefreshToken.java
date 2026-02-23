package br.ce.clinica.security;

import br.ce.clinica.dto.request.RefreshTokenRequest;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.keys.ReactiveKeyCommands;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.UUID;

@ApplicationScoped
public class RefreshToken {


    @ConfigProperty(name = "refresh.token.ttl")
    Long refreshTokenTTL;

    @Inject
    ReactiveRedisDataSource redisDataSource;

    public Uni<String> generateRefreshToken(Long usuarioId) {

        String refreshToken = UUID.randomUUID().toString();

        ReactiveValueCommands<String, String> values =
                redisDataSource.value(String.class);

        return values.setex(
                "refresh:" + refreshToken,
                refreshTokenTTL,
                usuarioId.toString()
        ).replaceWith(refreshToken);
    }

    public Uni<Long> validateRefreshToken(String token) {

        ReactiveValueCommands<String, String> values =
                redisDataSource.value(String.class);

        return values.get("refresh:" + token)
                .onItem().ifNull().failWith(() ->
                        new UnauthorizedException("Refresh token inválido ou expirado"))
                .map(Long::valueOf);
    }

    public Uni<Void> revokeRefreshToken(RefreshTokenRequest token) {

        ReactiveKeyCommands<String> keys =
                redisDataSource.key(String.class);

        return keys.del("refresh:" + token).replaceWithVoid();
    }

}