package br.ce.clinica.entity;

import br.ce.clinica.enums.TipoAntecedenteFamiliar;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@Table(name = "antecedente_familiar", schema = "clinica")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class AntecedenteFamiliar extends BaseAuditEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anamnese_id", nullable = false)
    private Anamnese anamnese;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "antecedente_familiar_tipo",
            schema = "clinica",
            joinColumns = @JoinColumn(name = "antecedente_familiar_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_antecedente_familiar")
    private Set<TipoAntecedenteFamiliar> tiposAntecedentes;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

}
