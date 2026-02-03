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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anamnese_id", nullable = false)
    private Anamnese anamnese;

    @ElementCollection(targetClass = TipoAntecedenteFamiliar.class)
    @CollectionTable(
            name = "antecedente_familiar_tipo",
            schema = "clinica",
            joinColumns = @JoinColumn(name = "antecedente_familiar_id")
    )
    @Column(name = "tipo_antecedente_familiar")
    @Enumerated(EnumType.STRING)
    private Set<TipoAntecedenteFamiliar> tipoAntecedenteFamiliar;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

}
