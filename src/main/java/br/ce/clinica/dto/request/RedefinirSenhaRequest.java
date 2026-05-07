package br.ce.clinica.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RedefinirSenhaRequest {

    @NotBlank(message = "Código é obrigatório")
    @Pattern(regexp = "\\d{6}", message = "Código deve conter exatamente 6 caracteres alfanuméricos")
    private String codigo;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "A senha deve ter pelo menos 8 caracteres, incluindo letras maiúsculas, minúsculas, números e caracteres especiais."
    )
    private String senha;
}
