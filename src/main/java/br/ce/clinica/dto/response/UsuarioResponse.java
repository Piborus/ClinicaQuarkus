package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Usuario;
import br.ce.clinica.enums.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class UsuarioResponse {

    private Long id;

    private String nome;

    private String sobrenome;

    private String email;

    private TipoUsuario tipoUsuario;

    public static UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .sobrenome(usuario.getSobrenome())
                .email(usuario.getEmail())
                .tipoUsuario(usuario.getTipoUsuario())
                .build();
    }
}
