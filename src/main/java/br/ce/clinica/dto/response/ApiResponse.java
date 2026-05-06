package br.ce.clinica.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ApiResponse {
    private String message;
}
