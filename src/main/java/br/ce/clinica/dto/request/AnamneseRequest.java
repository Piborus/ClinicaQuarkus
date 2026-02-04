package br.ce.clinica.dto.request;

import br.ce.clinica.enums.TipoAnamnese;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnamneseRequest {

    @Schema(name = "pacienteId", description = "Id do paciente", examples = {"1"})
    private Long pacienteId;

    private TipoAnamnese tipoAnamnese;

    private String encaminhamento;

    private String historicoAcompanhamento;

    private String psicodinamicaFamiliar;

    private String observacao;

    private AnamneseDesenvolvimentoRequest desenvolvimento;

    private AntecedenteFamiliarRequest antecedenteFamiliar;
}
