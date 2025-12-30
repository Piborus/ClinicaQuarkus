package br.ce.clinica.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "relatorio_do_paciente", schema = "clinica")
@EqualsAndHashCode(of = "id")
public class RelatorioDoPaciente extends BaseAuditEntity {

    @Column(name = "relatorio", columnDefinition = "TEXT")
    private String relatorio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;
}
