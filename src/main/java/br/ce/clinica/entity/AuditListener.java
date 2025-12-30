package br.ce.clinica.entity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.OffsetDateTime;

@ApplicationScoped
public class AuditListener {

    public AuditListener() {}

    @PrePersist
    public void setCreationDate(BaseAuditEntity baseAuditEntity) {
        OffsetDateTime agora = OffsetDateTime.now();
        baseAuditEntity.setDataCriacao(agora);
        baseAuditEntity.setDataAtualizacao(agora);
        baseAuditEntity.setStatus(true);
        baseAuditEntity.setDeletado(false);

        baseAuditEntity.setCriadoPor("SYSTEM");
        baseAuditEntity.setAtualizadoPor("SYSTEM");
    }

    @PreUpdate
    public void setUpdateDate(BaseAuditEntity baseAuditEntity) {
        baseAuditEntity.setDataAtualizacao(OffsetDateTime.now());
        baseAuditEntity.setAtualizadoPor("SYSTEM");
    }

}
