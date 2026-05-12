package br.ce.clinica.security;

import br.ce.clinica.exception.BadRequestBusinessException;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.keys.ReactiveKeyCommands;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
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

    public Uni<Void> salvarCodigo(Long usuarioId, String codigo) {

        ReactiveValueCommands<String, String> valueCommands = redisDataSource.value(String.class, String.class);
        ReactiveKeyCommands<String> keyCommands = redisDataSource.key(String.class);

        String chaveCodigo = montarChaveCodigo(usuarioId);
        String chaveTentativas = montarChaveTentativas(usuarioId);

        String codigoHash = hashService.hash(codigo);

        return valueCommands.set(chaveCodigo, codigoHash)
                .onItem()
                .transformToUni(result -> keyCommands.expire(chaveCodigo, TEMPO_EXPIRACAO))
                .onItem()
                .transformToUni(result -> valueCommands.set(chaveTentativas, "0"))
                .onItem()
                .transformToUni(result -> keyCommands.expire(chaveTentativas, TEMPO_EXPIRACAO))
                .replaceWithVoid();

    }

    public Uni<Boolean> codigoValido(Long usuarioId, String codigoInformado) {

        ReactiveValueCommands<String, String> valueCommands = redisDataSource.value(String.class, String.class);

        String chaveCodigo = montarChaveCodigo(usuarioId);
        //String codioHashInformado = hashService.hash(codigoInformado);

        return valueCommands.get(chaveCodigo)
                .onItem().ifNull().failWith(() -> new BadRequestBusinessException("Código inválido ou expirado."))
                .onItem().transform(codigoHashArmazenado -> hashService.verify(codigoInformado, codigoHashArmazenado));

    }


    public Uni<Long> incrementarTentativas(Long usuarioId) {

        ReactiveValueCommands<String, String> valueCommands = redisDataSource.value(String.class, String.class);
        ReactiveKeyCommands<String> keyCommands = redisDataSource.key(String.class);

        String chaveTentativas = montarChaveTentativas(usuarioId);

        return valueCommands.incr(chaveTentativas)
                .onItem().transformToUni(totalTentativas -> keyCommands.ttl(chaveTentativas)
                        .onItem().transformToUni(ttl -> {
                                    if (ttl != null && ttl == -1) {
                                        return keyCommands.expire(chaveTentativas, TEMPO_EXPIRACAO)
                                                .replaceWith(totalTentativas);
                                    }
                                    return Uni.createFrom().item(totalTentativas);
                                }
                        )
                );
    }

    public Uni<Boolean> tentativasExcedidas(Long usuarioId) {

        ReactiveValueCommands<String, String> valueCommands = redisDataSource.value(String.class, String.class);

        String chaveTentativas = montarChaveTentativas(usuarioId);

        return valueCommands.get(chaveTentativas)
                .onItem().transform(
                        valor -> {
                            if (valor == null) {
                                return false;
                            }
                            try {
                                long tentativas = Long.parseLong(valor);
                                return tentativas >= LIMITE_TENTATIVAS;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        }
                );
    }

    public Uni<Void> removerCodigo(Long usuarioId) {
        ReactiveKeyCommands<String> keyCommands = redisDataSource.key(String.class);

        return keyCommands.del(
                montarChaveCodigo(usuarioId),
                montarChaveTentativas(usuarioId)
        ).replaceWithVoid();
    }

    private String montarChaveCodigo(Long usuarioId) {
        return PREFIXO_CODIGO + usuarioId;
    }
    private String montarChaveTentativas(Long usuarioId) {
        return PREFIXO_TENTATIVAS + usuarioId;
    }

}
