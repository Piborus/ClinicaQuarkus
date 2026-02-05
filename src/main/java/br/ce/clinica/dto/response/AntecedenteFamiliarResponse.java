package br.ce.clinica.dto.response;

import br.ce.clinica.entity.AntecedenteFamiliar;
import br.ce.clinica.enums.TipoAntecedenteFamiliar;
import lombok.*;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AntecedenteFamiliarResponse {

    private Long id;

    private List<TipoAntecedenteFamiliar> tiposAntecedentes;

    private String descricao;

    public static AntecedenteFamiliarResponse toResponse(AntecedenteFamiliar antecedenteFamiliar) {
        return AntecedenteFamiliarResponse.builder()
                .id(antecedenteFamiliar.getId())
                .tiposAntecedentes(
                        antecedenteFamiliar.getTiposAntecedentes() == null
                                ? Collections.emptyList()
                                : List.copyOf(antecedenteFamiliar.getTiposAntecedentes())
                )
                .descricao(antecedenteFamiliar.getDescricao())
                .build();
    }

}
