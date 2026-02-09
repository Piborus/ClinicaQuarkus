package br.ce.clinica.dto.response;

import br.ce.clinica.entity.Anamnese;
import br.ce.clinica.enums.TipoAnamnese;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnamneseResponse {

    private PacienteResponse paciente;

    private Long id;

    private TipoAnamnese tipoAnamnese;

    private String encaminhamento;

    private String historicoAcompanhamento;

    private String psicodinamicaFamiliar;

    private String observacao;

    private AnamneseDesenvolvimentoResponse desenvolvimento;

    private AntecedenteFamiliarResponse antecedenteFamiliar;


    public static AnamneseResponse toResponse(Anamnese anamnese) {
        return AnamneseResponse.builder()
                .id(anamnese.getId())
                .tipoAnamnese(anamnese.getTipoAnamnese())
                .encaminhamento(anamnese.getEncaminhamento())
                .historicoAcompanhamento(anamnese.getHistoricoAcompanhamento())
                .psicodinamicaFamiliar(anamnese.getPsicodinamicaFamiliar())
                .observacao(anamnese.getObservacao())
                .paciente(
                        anamnese.getPaciente() != null
                                ? PacienteResponse.toResponse(anamnese.getPaciente())
                                : null
                )
                .desenvolvimento(
                        anamnese.getDesenvolvimento() != null
                                ? AnamneseDesenvolvimentoResponse.toResponse(anamnese.getDesenvolvimento())
                                : null
                )
                .antecedenteFamiliar(
                        anamnese.getAntecedenteFamiliar() != null
                                ? AntecedenteFamiliarResponse.toResponse(anamnese.getAntecedenteFamiliar())
                                : null
                )
                .build();
    }


}
