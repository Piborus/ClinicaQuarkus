package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.LembreteDeConsultaRequest;
import io.quarkus.mailer.MailTemplate;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailServiceImpl Unit Tests")
class EmailServiceImplTest {

    @Mock
    MailTemplate template;

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl();
//        emailService.template = template;
    }

    @Test
    @DisplayName("Deve enviar lembrete de consulta com sucesso")
    void enviarLembreConsultaComSucesso() {
        MailTemplate.MailTemplateInstance instance = mock(MailTemplate.MailTemplateInstance.class);

        when(template.to("paciente@clinica.com")).thenReturn(instance);
        when(instance.subject("Lembrete de Consulta")).thenReturn(instance);
        when(instance.data(eq("nomePaciente"), eq("Maria"))).thenReturn(instance);
        when(instance.data(eq("nomeProfissional"), eq("Dra. Ana"))).thenReturn(instance);
        when(instance.data(eq("dataConsulta"), eq("20/04/2026"))).thenReturn(instance);
        when(instance.data(eq("horaConsulta"), eq("14:00"))).thenReturn(instance);
        when(instance.send()).thenReturn(Uni.createFrom().voidItem());

        String result = emailService.enviarLembreConsulta(
                        "paciente@clinica.com",
                        "Maria",
                        "Dra. Ana",
                        "20/04/2026",
                        "14:00"
                )
                .await().indefinitely();

        assertEquals("Lembrete enviado com sucesso para paciente@clinica.com", result);
        verify(template).to("paciente@clinica.com");
        verify(instance).subject("Lembrete de Consulta");
        verify(instance).data("nomePaciente", "Maria");
        verify(instance).data("nomeProfissional", "Dra. Ana");
        verify(instance).data("dataConsulta", "20/04/2026");
        verify(instance).data("horaConsulta", "14:00");
        verify(instance).send();
    }

    @Test
    @DisplayName("Deve montar template completo ao mandar lembrete")
    void mandarLembreConsultaComSucesso() {
        MailTemplate.MailTemplateInstance instance = mock(MailTemplate.MailTemplateInstance.class);
        LembreteDeConsultaRequest request = LembreteDeConsultaRequest.builder()
                .destinatario("paciente@clinica.com")
                .nomePaciente("Maria")
                .nomeProfissional("Dra. Ana")
                .especialidade("Psicologia")
                .dataConsulta("20/04/2026")
                .horaConsulta("14:00")
                .build();

        when(template.to(request.getDestinatario())).thenReturn(instance);
        when(instance.subject("Lembrete de Consulta")).thenReturn(instance);
        when(instance.data(eq("nomePaciente"), eq(request.getNomePaciente()))).thenReturn(instance);
        when(instance.data(eq("nomeProfissional"), eq(request.getNomeProfissional()))).thenReturn(instance);
        when(instance.data(eq("especialidade"), eq(request.getEspecialidade()))).thenReturn(instance);
        when(instance.data(eq("dataConsulta"), eq(request.getDataConsulta()))).thenReturn(instance);
        when(instance.data(eq("horaConsulta"), eq(request.getHoraConsulta()))).thenReturn(instance);
        when(instance.send()).thenReturn(Uni.createFrom().voidItem());

        emailService.mandarLembreConsulta(request).await().indefinitely();

        verify(template).to(request.getDestinatario());
        verify(instance).subject("Lembrete de Consulta");
        verify(instance).data("nomePaciente", request.getNomePaciente());
        verify(instance).data("nomeProfissional", request.getNomeProfissional());
        verify(instance).data("especialidade", request.getEspecialidade());
        verify(instance).data("dataConsulta", request.getDataConsulta());
        verify(instance).data("horaConsulta", request.getHoraConsulta());
        verify(instance).send();
    }
}
