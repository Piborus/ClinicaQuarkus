package br.ce.clinica.dto.request;

import br.ce.clinica.enums.TipoDePagamento;
import br.ce.clinica.enums.TipoMovimento;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransacaoRequest {

    @Schema(name = "valor", description = "Valor da transação", examples = { "150.75" })
    private Double valor;

    @Schema(name = "descricao", description = "Descrição da transação", examples = { "Pagamento de consulta médica" })
    private String descricao;

    @Schema(name = "tipoMovimento", description = "Tipo de movimento", examples = { "ENTRADA" })
    private TipoMovimento tipoMovimento;

    @Schema(name = "tipoDePagamento", description = "Tipo de pagamento", examples = { "DINHEIRO" })
    private TipoDePagamento tipoDePagamento;

    @Schema(name = "pacienteId", description = "Id do paciente", examples = { "1" })
    private Long pacienteId;

}
