package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Transacao;
import br.ce.clinica.enums.TipoDePagamento;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransacaoResponse {

    private Long id;

    private Integer caixaEntrada;

    private Integer caixaSaida;

    private String descricao;

    private TipoDePagamento tipoDePagamento;

    private PacienteResponse paciente;

    public static TransacaoResponse fromEntity(Transacao transacao) {
        return TransacaoResponse.builder()
                .id(transacao.getId())
                .caixaEntrada(transacao.getCaixaEntrada())
                .caixaSaida(transacao.getCaixaSaida())
                .descricao(transacao.getDescricao())
                .tipoDePagamento(transacao.getTipoDePagamento())
                .paciente(PacienteResponse.fromEntity(transacao.getPaciente()))
                .build();
    }
}
