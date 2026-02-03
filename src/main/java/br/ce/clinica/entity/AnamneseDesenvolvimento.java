package br.ce.clinica.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "anamnese_desenvolvimento", schema = "clinica")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class AnamneseDesenvolvimento extends BaseAuditEntity {

    @Column(name = "gravidez_parto", columnDefinition = "TEXT")
    private String gravidezParto;

    @Column(name = "memorias_infancia", columnDefinition = "TEXT")
    private String memoriasInfancia;

    @Column(name = "memorias_adolescencia", columnDefinition = "TEXT")
    private String memoriasAdolescencia;

    @Column(name = "fase_adulta", columnDefinition = "TEXT")
    private String faseAdulta;

    @Column(name = "fase_atual", columnDefinition = "TEXT")
    private String faseAtual;

    @Column(name = "mora_com_quem", columnDefinition = "TEXT")
    private String moraComQuem;

    @Column(name = "numero_filhos")
    private Integer numeroFilhos;

    @Column(name = "numero_irmaos")
    private Integer numeroIrmaos;

    @Column(name = "ordem_nascimento")
    private String ordemNascimento;

    @Column(name = "fumante")
    private Boolean fumante;

    @Column(name = "etilista")
    private Boolean etilista;

    @Column(name = "uso_medicamentos")
    private Boolean usoMedicamentos;

    @Column(name = "descricao_medicamentos", columnDefinition = "TEXT")
    private String descricaoMedicamentos;

    @Column(name = "rotina", columnDefinition = "TEXT")
    private String rotina;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anamnese_id", nullable = false, unique = true)
    private Anamnese anamnese;
}
