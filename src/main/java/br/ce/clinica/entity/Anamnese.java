package br.ce.clinica.entity;

import br.ce.clinica.enums.TipoAnamnese;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@Table(name = "anamnese", schema = "clinica")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Anamnese extends BaseAuditEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_anamnese")
    private TipoAnamnese tipoAnamnese;

    @Column(name = "encaminhamento", columnDefinition = "TEXT")
    private String encaminhamento;

    @Column(name = "historico_acompanhamento", columnDefinition = "TEXT")
    private String historicoAcompanhamento;

    @Column(name = "psicodinamica_familiar", columnDefinition = "TEXT")
    private String psicodinamicaFamiliar;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @OneToOne(mappedBy = "anamnese", cascade = CascadeType.ALL, orphanRemoval = true)
    private AnamneseDesenvolvimento desenvolvimento;

    @OneToOne(mappedBy = "anamnese", cascade = CascadeType.ALL, orphanRemoval = true)
    private AntecedenteFamiliar antecedenteFamiliar;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;
}
