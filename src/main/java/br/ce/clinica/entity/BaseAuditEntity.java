package br.ce.clinica.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditListener.class)
public abstract class BaseAuditEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dt_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    @Column(name = "dt_alteracao")
    private OffsetDateTime dataAtualizacao;

    @Column(name = "dt_delecao")
    private OffsetDateTime dataDelecao;

    @Column(name = "criado_por", length = 100, updatable = false)
    private String criadoPor;

    @Column(name = "atualizado_por", length = 100)
    private String atualizadoPor;

    @Column(name = "status", nullable = false)
    private Boolean status;

    @Column(name = "deletado", nullable = false)
    private Boolean deletado;
}
