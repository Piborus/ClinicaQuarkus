package br.ce.clinica.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.smallrye.common.constraint.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgendaRequest {

    @NotNull
    private Long idpaciente;

    @NotNull
    private Long idUsuario;

    @NotNull
    @FutureOrPresent(message = "Horário deve ser no presente ou futuro")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Schema(description = "Data e hora da consulta", examples = {"25/12/2024 14:30:00"})
    private LocalDateTime horario;
}
