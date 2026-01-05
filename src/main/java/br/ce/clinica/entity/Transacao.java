package br.ce.clinica.entity;

import br.ce.clinica.enums.TipoDePagamento;
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

    @Column(name = "caixa_entrada")
    private Integer caixaEntrada;

    @Column(name = "caixa_saida")
    private Integer caixaSaida;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "tipo_de_pagamento")
    @Enumerated(EnumType.STRING)
    private TipoDePagamento tipoDePagamento;

    @ManyToOne()
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

}
