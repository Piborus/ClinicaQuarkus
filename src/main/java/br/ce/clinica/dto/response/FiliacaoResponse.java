package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Filiacao;
import br.ce.clinica.enums.GrauParentesco;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FiliacaoResponse {

    private Long id;

    private String nome;

    private Integer idade;

    private String cpf;

    private String telefone;

    private String email;

    private GrauParentesco grauDeParentesco;

    public static FiliacaoResponse toResponse(Filiacao filiacao){
        return FiliacaoResponse.builder()
                .id(filiacao.getId())
                .nome(filiacao.getNome())
                .idade(filiacao.getIdade())
                .cpf(filiacao.getCpf())
                .telefone(filiacao.getTelefone())
                .email(filiacao.getEmail())
                .grauDeParentesco(filiacao.getGrauDeParentesco())
                .build();
    }

}
