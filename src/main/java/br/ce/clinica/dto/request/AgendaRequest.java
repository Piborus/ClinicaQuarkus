package br.ce.clinica.dto.request;

import br.ce.clinica.entity.Consulta;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgendaRequest {

    private Long idpaciente;

    private Long idUsuario;

    private LocalDateTime horario;

    public static AgendaRequest toRequest(Consulta consulta) {
        return AgendaRequest.builder()
                .idpaciente(consulta.getPaciente().getId())
                .idUsuario(consulta.getUsuario().getId())
                .horario(consulta.getDataInicio())
                .build();
    }
}
