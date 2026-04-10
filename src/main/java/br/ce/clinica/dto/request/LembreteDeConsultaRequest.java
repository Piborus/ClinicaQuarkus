package br.ce.clinica.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LembreteDeConsultaRequest {

    private String destinatario;

    private String nomePaciente;

    private String nomeProfissional;

    private String especialidade;

    private String dataConsulta;

    private String horaConsulta;
}
