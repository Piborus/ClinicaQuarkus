package br.ce.clinica.entity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.OffsetDateTime;

@ApplicationScoped
public class AuditListener {

    private static final String USUARIO_SISTEMA = "system";

    @Inject
    JsonWebToken jwt;

    public AuditListener() {
    }

    @PrePersist
    public void setCreationDate(BaseAuditEntity baseAuditEntity) {
        OffsetDateTime agora = OffsetDateTime.now();

        baseAuditEntity.setDataCriacao(agora);
        baseAuditEntity.setDataAtualizacao(agora);

        if (baseAuditEntity.getStatus() == null) {
            baseAuditEntity.setStatus(true);
        }

        if (baseAuditEntity.getDeletado() == null) {
            baseAuditEntity.setDeletado(false);
        }

        String usuarioAtual = obterUsuarioAtual();

        baseAuditEntity.setCriadoPor(usuarioAtual);
        baseAuditEntity.setAtualizadoPor(usuarioAtual);
    }

    @PreUpdate
    public void setUpdateDate(BaseAuditEntity baseAuditEntity) {
        baseAuditEntity.setDataAtualizacao(OffsetDateTime.now());
        baseAuditEntity.setAtualizadoPor(obterUsuarioAtual());
    }

    String obterUsuarioAtual() {
        try {
            if (jwt == null) {
                return USUARIO_SISTEMA;
            }

            String usuarioId = jwt.getSubject();

            if (usuarioId == null || usuarioId.isBlank()) {
                usuarioId = jwt.getClaim("usuario_id");
            }

            return usuarioId == null || usuarioId.isBlank()
                    ? USUARIO_SISTEMA
                    : usuarioId;

        } catch (Exception e) {
            return USUARIO_SISTEMA;
        }
    }


//    @PreUpdate
//    public void setDeleteDate(BaseAuditEntity baseAuditEntity) {
//        baseAuditEntity.setDataDelecao(OffsetDateTime.now());
//        baseAuditEntity.setDeletado(true);
//        baseAuditEntity.setStatus(false);
//    }

}
