package br.ce.clinica.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "relatorio", schema = "clinica")
@EqualsAndHashCode(of = "id")
public class Relatorio extends BaseAuditEntity {

    @Column(name = "texto", columnDefinition = "TEXT")
    private String texto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;
}
