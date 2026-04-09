package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Carteira;
import br.ce.clinica.enums.TipoDePagamento;
import br.ce.clinica.enums.TipoMovimento;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarteiraResumeResponse {

    private Long id;

    private BigDecimal valor;

    private String descricao;

    private TipoMovimento tipoMovimento;

    private TipoDePagamento tipoDePagamento;

    public static CarteiraResumeResponse toResponse(Carteira carteira) {
        return CarteiraResumeResponse.builder()
                .id(carteira.getId())
                .valor(carteira.getValor())
                .descricao(carteira.getDescricao())
                .tipoMovimento(carteira.getTipoMovimento())
                .tipoDePagamento(carteira.getTipoDePagamento())
                .build();
    }
}
