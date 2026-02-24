package br.ce.clinica.dto.request;

import br.ce.clinica.entity.Consulta;
import br.ce.clinica.enums.StatusConfirmacao;
import br.ce.clinica.enums.StatusConsulta;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsultaRequest {

    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;

    private String observacao;

    private Long idPaciente;

    private Long idUsuario;

    private StatusConsulta statusConsulta;

    private StatusConfirmacao statusConfirmacao;

    public static ConsultaRequest toRequest(Consulta consulta) {
        return ConsultaRequest.builder()
                .dataInicio(consulta.getDataInicio())
                .dataFim(consulta.getDataFim())
                .observacao(consulta.getObservacao())
                .statusConsulta(consulta.getStatusConsulta())
                .statusConfirmacao(consulta.getStatusConfirmacao())
                .idPaciente(consulta.getPaciente().getId())
                .idUsuario(consulta.getUsuario().getId())
                .build();
    }

}
