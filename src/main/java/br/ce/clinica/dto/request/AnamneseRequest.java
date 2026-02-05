package br.ce.clinica.dto.request;

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

    //private TipoAnamnese tipoAnamnese;

    @Schema(name = "encaminhamento", description = "Encaminhamento do paciente", examples = {"Hospital"})
    private String encaminhamento;

    @Schema(name = "historicoAcompanhamento", description = "Histórico de acompanhamento do paciente", examples = {"Paciente acompanhado por psicólogo"})
    private String historicoAcompanhamento;

    @Schema(name = "psicodinamicaFamiliar", description = "Psicodinamica do paciente", examples = {"Paciente com problemas de autoconhecimento"})
    private String psicodinamicaFamiliar;

    @Schema(name = "observacao", description = "Observacao do paciente", examples = {"Paciente com problemas de autoconhecimento"})
    private String observacao;

    private AnamneseDesenvolvimentoRequest desenvolvimento;

    private AntecedenteFamiliarRequest antecedenteFamiliar;
}
