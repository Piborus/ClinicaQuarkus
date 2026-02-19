package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Endereco;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnderecoResponse {

    private String rua;

    private String numero;

    private String bairro;

    private String cep;

    private String complemento;

    private String cidade;

    private String estado;

    private String pais;

    public static EnderecoResponse toResponse(Endereco endereco) {
        if (endereco == null) {
            return null;
        }
        return EnderecoResponse.builder()
                .rua(endereco.getLogradouro())
                .numero(endereco.getNumero())
                .bairro(endereco.getBairro())
                .cep(endereco.getCep())
                .complemento(endereco.getComplemento())
                .cidade(endereco.getCidade())
                .estado(endereco.getEstado())
                .pais(endereco.getPais())
                .build();
    }
}


