package br.ce.clinica.entity;

import br.ce.clinica.enums.GrauParentesco;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.validator.constraints.br.CPF;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "filiacao", schema = "clinica")
@EqualsAndHashCode(of = "id")
public class Filiacao extends BaseAuditEntity {

    @Column(name = "nome")
    private String nome;

    @Column(name = "idade")
    private Integer idade;

    @CPF
    @Column(name = "cpf")
    private String cpf;

    @Column(name = "telefone")
    private String telefone;

    @Email
    @Column(name = "email")
    private String email;

    @Column(name = "grau_parentesco")
    @Enumerated(EnumType.STRING)
    private GrauParentesco grauDeParentesco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;
}
