package br.ce.clinica.entity;

import br.ce.clinica.enums.TipoDePagamento;
import br.ce.clinica.enums.TipoMovimento;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "transacao", schema = "clinica")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Transacao extends BaseAuditEntity{

    @Column(name = "valor", nullable = false)
    private Double valor;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "tipo_movimento", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoMovimento tipoMovimento;

    @Column(name = "tipo_pagamento", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoDePagamento tipoDePagamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

}
