package br.ce.clinica.service;

import br.ce.clinica.dto.request.LembreteDeConsultaRequest;
import io.smallrye.mutiny.Uni;

import java.time.LocalDate;

public interface EmailService {

    Uni<String> enviarLembreConsulta(
            String destinatario,
            String nomePaciente,
            String nomeProfissional,
            String dataConsulta,
            String horaConsulta
    );

    Uni<Void> mandarLembreConsulta(LembreteDeConsultaRequest request);

    Uni<Void> enviarEmailRecuperacaoSenha(String email, String nomeUsuario, String codigo);

    Uni<Void> enviarEmailBemVindo(String email, String nomeUsuario, LocalDate dataCriacao);
}
