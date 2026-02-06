package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Prontuario;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProntuarioResumeResponse {

    private Long id;

    private String texto;

    private PacienteResponse paciente;

    public static ProntuarioResumeResponse toResponse(Prontuario prontuario) {
        return ProntuarioResumeResponse.builder()
                .id(prontuario.getId())
                .texto(prontuario.getTexto())
                .paciente(PacienteResponse.toResponse(prontuario.getPaciente()))
                .build();
    }
}
