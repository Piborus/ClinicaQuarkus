package br.ce.clinica.security;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.keys.ReactiveKeyCommands;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class RefreshToken {

    private static final long REFRESH_TOKEN_TTL = 604800;

    @Inject
    ReactiveRedisDataSource redisDataSource;

    public Uni<String> generateRefreshToken(Long usuarioId) {

        String refreshToken = UUID.randomUUID().toString();

        ReactiveValueCommands<String, String> values =
                redisDataSource.value(String.class);

        return values.setex(
                "refresh:" + refreshToken,
                REFRESH_TOKEN_TTL,
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

    public Uni<Void> revokeRefreshToken(String token) {

        ReactiveKeyCommands<String> keys =
                redisDataSource.key(String.class);

        return keys.del("refresh:" + token).replaceWithVoid();
    }

}
//c8036d6b-9481-45a5-9335-e882d7df15de token para testa