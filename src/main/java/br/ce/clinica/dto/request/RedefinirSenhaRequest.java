package br.ce.clinica.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Builder
@Getter
public class RedefinirSenhaRequest {

    @NotBlank(message = "Código é obrigatório")
    @Pattern(regexp = "\\d{6}", message = "Código deve conter exatamente 6 caracteres alfanuméricos")
    @Schema(description = "Código de recuperação enviado por email", examples = {"123456"})
    private String codigo;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Schema(description = "Email do usuário para o qual a senha será redefinida", examples = {"demo@email.com"})
    private String email;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "A senha deve ter pelo menos 8 caracteres, incluindo letras maiúsculas, minúsculas, números e caracteres especiais."
    )
    @Schema(description = "Nova senha do usuário.", examples = {"NovaSenha@123"})
    private String novaSenha;
}
