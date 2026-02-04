package br.ce.clinica.dto.request;

import br.ce.clinica.enums.TipoAntecedenteFamiliar;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AntecedenteFamiliarRequest {

    private List<TipoAntecedenteFamiliar> tiposAntecedentes;

    private String descricao;

}
