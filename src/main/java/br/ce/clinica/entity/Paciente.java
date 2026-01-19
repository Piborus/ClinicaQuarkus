package br.ce.clinica.entity;

import br.ce.clinica.enums.Sexo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "paciente", schema = "clinica")
@EqualsAndHashCode(of = "id")
public class Paciente extends BaseAuditEntity {

    @Column(name = "nome", nullable = false)
    @NotBlank
    private String nome;

    @Column(name = "idade")
    private Integer idade;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo")
    private Sexo sexo;

    @NotNull
    @PastOrPresent
    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "cpf", unique = true)
    private String cpf;

    @Column(name = "rg", unique = true)
    private String rg;

    @Column(name = "telefone")
    private String telefone;

    @Column(name = "email")
    private String email;

    @Embedded
    private Endereco endereco;

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Relatorio> relatorioDoPaciente;

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Transacao> transacao;

}
