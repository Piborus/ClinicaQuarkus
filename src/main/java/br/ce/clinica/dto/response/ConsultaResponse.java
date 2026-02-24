package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Consulta;
import br.ce.clinica.enums.StatusConfirmacao;
import br.ce.clinica.enums.StatusConsulta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsultaResponse {

    private Long id;

    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;

    private StatusConsulta statusConsulta;

    private StatusConfirmacao statusConfirmacao;

    private String observacao;

    private PacienteResumeResponse paciente;

    private UsuarioResponse usuario;

    public static ConsultaResponse toResponse(Consulta consulta) {
        return ConsultaResponse.builder()
                .id(consulta.getId())
                .dataInicio(consulta.getDataInicio())
                .dataFim(consulta.getDataFim())
                .statusConsulta(consulta.getStatusConsulta())
                .statusConfirmacao(consulta.getStatusConfirmacao())
                .observacao(consulta.getObservacao())
                .paciente(PacienteResumeResponse.toResponse(consulta.getPaciente()))
                .usuario(UsuarioResponse.toResponse(consulta.getUsuario()))
                .build();
    }


}
