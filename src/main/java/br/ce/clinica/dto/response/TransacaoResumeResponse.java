package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Transacao;
import br.ce.clinica.enums.TipoDePagamento;
import br.ce.clinica.enums.TipoMovimento;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransacaoResumeResponse {

    private Long id;

    private Double valor;

    private String descricao;

    private TipoMovimento tipoMovimento;

    private TipoDePagamento tipoDePagamento;

    public static TransacaoResumeResponse toResponse(Transacao transacao) {
        return TransacaoResumeResponse.builder()
                .id(transacao.getId())
                .valor(transacao.getValor())
                .descricao(transacao.getDescricao())
                .tipoMovimento(transacao.getTipoMovimento())
                .tipoDePagamento(transacao.getTipoDePagamento())
                .build();
    }
}
