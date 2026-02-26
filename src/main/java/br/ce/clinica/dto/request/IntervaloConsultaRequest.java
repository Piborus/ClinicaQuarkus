package br.ce.clinica.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntervaloConsultaRequest {

    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;
}
