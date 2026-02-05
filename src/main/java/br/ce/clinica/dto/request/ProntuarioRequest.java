package br.ce.clinica.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProntuarioRequest {

    @Schema(name = "texto",
            description = "Prontuario do paciente",
            examples = { "Paciente evolui sem intercorrencias relevantes desde a ultima consulta." })
    @NotBlank
    private String texto;

    @Schema(name = "pacienteId", description = "Id do paciente", examples = { "1" })
    @NotNull
    private Long pacienteId;

}
