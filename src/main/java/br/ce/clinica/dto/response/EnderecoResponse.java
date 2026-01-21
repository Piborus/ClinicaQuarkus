package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Endereco;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
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


