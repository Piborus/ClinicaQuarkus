package br.ce.clinica.dto.request;

import br.ce.clinica.enums.TipoDePagamento;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransacaoRequest {

    private Integer caixaEntrada;

    private Integer caixaSaida;

    private String descricao;

    private TipoDePagamento tipoDePagamento;

    private Long pacienteId;

}
