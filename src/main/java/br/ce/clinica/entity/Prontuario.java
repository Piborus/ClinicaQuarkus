package br.ce.clinica.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "prontuario", schema = "clinica")
@EqualsAndHashCode(of = "id")
public class Prontuario extends BaseAuditEntity {

    @Column(name = "texto", columnDefinition = "TEXT")
    private String texto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;
}
