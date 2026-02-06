package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Carteira;
import br.ce.clinica.enums.TipoDePagamento;
import br.ce.clinica.enums.TipoMovimento;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarteiraResumeResponse {

    private Long id;

    private Double valor;

    private String descricao;

    private TipoMovimento tipoMovimento;

    private TipoDePagamento tipoDePagamento;

    private PacienterResponse paciente;

    public static CarteiraResumeResponse toResponse(Carteira carteira) {
        return CarteiraResumeResponse.builder()
                .id(carteira.getId())
                .valor(carteira.getValor())
                .descricao(carteira.getDescricao())
                .tipoMovimento(carteira.getTipoMovimento())
                .tipoDePagamento(carteira.getTipoDePagamento())
                .paciente(PacienterResponse.toResponse(carteira.getPaciente()))
                .build();
    }
}
