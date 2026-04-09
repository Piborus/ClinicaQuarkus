package br.ce.clinica.entity;

import br.ce.clinica.enums.TipoUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

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

    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "senha", nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", nullable = false)
    private TipoUsuario tipoUsuario;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Consulta> consultas;

}
