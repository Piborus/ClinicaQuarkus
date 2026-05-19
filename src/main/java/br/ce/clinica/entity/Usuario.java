package br.ce.clinica.entity;

import br.ce.clinica.enums.Especialidade;
import br.ce.clinica.enums.PsicologiaAbordagem;
import br.ce.clinica.enums.TipoUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@Table(name = "usuario", schema = "clinica")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Usuario extends BaseAuditEntity {

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @CPF
    @Column(name = "cpf", unique = true)
    private String cpf;

    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "senha", nullable = false)
    private String senha;

    @Column(name = "telefone")
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", nullable = false)
    private TipoUsuario tipoUsuario;

    @Column(name = "crp", unique = true)
    private String crp;

    @Column(name = "especialidade")
    @Enumerated(EnumType.STRING)
    private Especialidade especialidade;

    @Column(name = "psicologia_abordagem")
    @Enumerated(EnumType.STRING)
    private PsicologiaAbordagem psicologiaAbordagem;
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Consulta> consultas;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Paciente> pacientes;

}
