package br.ce.clinica.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioRequest {

    @NotBlank
    @Schema(description = "Nome do usuário", examples = {"João"})
    private String nome;

    @NotBlank
    @Schema(description = " Sobrenome do usuário", examples = {"da Silva"})
    private String sobrenome;

    @NotNull
    @PastOrPresent(message = "A data de nascimento do usuário não pode ser no futuro.")
    @Schema(description = "Data de nascimento do usuário", examples = {"15/08/1990"})
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataNascimento;

    @CPF
    @NotBlank
    @Schema(description = "CPF do usuário", examples = {"123.456.789-00"})
    private String cpf;

    @Email
    @NotBlank
    @Schema(description = "Email do usuário", examples =  {"jda@email.com"})
    private String email;

    @NotBlank
    @Schema(description = "Senha do usuário", examples = {"senha123"})
    private String senha;

    @Schema(description = "Telefone do usuário", examples = {"(11) 91234-5678"})
    private String telefone;

    @Schema(description = "Numero Conselho Regional de Psicologia", examples = {"11/1112"})
    private String crp;

    @Schema(description = "Especialidade", examples = {"Psicologia não comportamental"})
    private String especialidade;

}
