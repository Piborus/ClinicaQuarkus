package br.ce.clinica.dto.response;

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

    private AnamneseDesenvolvimentoResponse anamneseDesenvolvimentoResponse;

    private AntecedenteFamiliarResponse antecedenteFamiliarResponse;


}
