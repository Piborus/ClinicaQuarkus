package br.ce.clinica.service;

import br.ce.clinica.dto.request.IntervaloConsultaRequest;
import br.ce.clinica.dto.request.LembreteDeConsultaRequest;
import io.smallrye.mutiny.Uni;

public interface EmailService {

    Uni<String> enviarLembreConsulta(
            String destinatario,
            String nomePaciente,
            String nomeProfissional,
            String dataConsulta,
            String horaConsulta
    );

    Uni<Void> mandarLembreConsulta(LembreteDeConsultaRequest request);
}
