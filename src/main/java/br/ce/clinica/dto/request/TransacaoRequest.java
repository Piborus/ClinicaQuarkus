package br.ce.clinica.dto.request;

import br.ce.clinica.enums.TipoDePagamento;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransacaoRequest {

    @Schema(name = "caixaEntrada", description = "Valor de entrada no caixa", examples = { "1500" })
    private Integer caixaEntrada;

    @Schema(name = "caixaSaida", description = "Valor de saída no caixa", examples = { "500" })
    private Integer caixaSaida;

    @Schema(name = "descricao", description = "Descrição da transação", examples = { "Pagamento de consulta médica" })
    private String descricao;

    @Schema(name = "tipoDePagamento", description = "Tipo de pagamento", examples = { "DINHEIRO" })
    private TipoDePagamento tipoDePagamento;

    @Schema(name = "pacienteId", description = "Id do paciente", examples = { "1" })
    private Long pacienteId;

}
