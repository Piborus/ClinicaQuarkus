package br.ce.clinica.dto.request;

import br.ce.clinica.enums.TipoAntecedenteFamiliar;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AntecedenteFamiliarRequest {

    private List<TipoAntecedenteFamiliar> tiposAntecedentes;

    @Schema(name = "descricao",
            description = "Descrição do antecedente familiar",
            examples = {"Pai com histórico de depressão"})
    private String descricao;

}
