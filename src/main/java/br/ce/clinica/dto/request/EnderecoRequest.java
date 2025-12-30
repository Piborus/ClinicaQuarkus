package br.ce.clinica.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
public class EnderecoRequest {

    @Schema(name = "rua", description = "Rua do paciente", examples = { "Av. Paulista" })
    private String rua;

    @Schema(name = "numero", description = "Número da residência do paciente", examples = { "123" })
    private String numero;

    @Schema(name = "bairro", description = "Bairro do paciente", examples = { "Bela Vista" })
    private String bairro;

    @Schema(name = "cep", description = "CEP do paciente", examples = { "01311-000" })
    private String cep;

    @Schema(name = "complemento", description = "Complemento do endereço do paciente", examples = { "Apto 45" })
    private String complemento;

    @Schema(name = "cidade", description = "Cidade do paciente", examples = { "São Paulo" })
    private String cidade;

    @Schema(name = "estado", description = "Estado do paciente", examples = { "SP" })
    private String estado;

    @Schema(name = "pais", description = "País do paciente", examples = { "Brasil" })
    private String pais;
}
