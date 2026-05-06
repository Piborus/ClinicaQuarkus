package br.ce.clinica.dto.request;

import br.ce.clinica.entity.Consulta;
import br.ce.clinica.enums.StatusConfirmacao;
import br.ce.clinica.enums.StatusConsulta;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgendaCancelamentoRequest {

    private String observacao;

    private StatusConsulta statusConsulta;

    private StatusConfirmacao statusConfirmacao;

}
