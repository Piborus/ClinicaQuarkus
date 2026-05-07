package br.ce.clinica.security;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;

@ApplicationScoped
public class RecuperacaoSenhaRedisService {

    private static final Duration TEMPO_EXPIRACAO = Duration.ofMinutes(10);
    private static final int LIMITE_TENTATIVAS = 5;

    private static final String PREFIXO_CODIGO = "password-reset:codigo:usuario:";
    private static final String PREFIXO_TENTATIVAS = "password-reset:tentativas:usuario:";

    @Inject
    ReactiveRedisDataSource redisDataSource;

    @Inject
    HashService hashService;

    public Uni<Void> salvarCodigo()

}
