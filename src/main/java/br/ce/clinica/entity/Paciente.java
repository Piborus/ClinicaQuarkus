package br.ce.clinica.entity;

import br.ce.clinica.enums.Escolaridade;
import br.ce.clinica.enums.EstadoCivil;
import br.ce.clinica.enums.Sexo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.hibernate.validator.constraints.br.CPF;
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

    @CPF
    @Column(name = "cpf", unique = true)
    private String cpf;

    @Column(name = "rg", unique = true)
    private String rg;

    @Column(name = "telefone")
    private String telefone;

    @Column(name = "email")
    private String email;

    @Column(name = "religiao")
    private String religiao;

    @Column(name = "naturalidade")
    private String naturalidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "escolaridade")
    private Escolaridade escolaridade;

    @Column(name = "profissao")
    private String profissao;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil")
    private EstadoCivil estadoCivil;

    @Embedded
    private Endereco endereco;

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Prontuario> prontuarioDoPaciente;

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Carteira> transacao;

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Filiacao> responsaveis;

}
