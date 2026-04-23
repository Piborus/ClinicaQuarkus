package br.ce.clinica.scheduler;

import br.ce.clinica.dto.request.LembreteDeConsultaRequest;
import br.ce.clinica.entity.Consulta;
import br.ce.clinica.entity.Paciente;
import br.ce.clinica.entity.Usuario;
import br.ce.clinica.enums.TipoUsuario;
import br.ce.clinica.repository.ConsultaRepository;
import br.ce.clinica.service.EmailService;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LembreteScheduler Unit Tests")
class LembreteSchedulerTest {

    @Mock
    ConsultaRepository consultaRepository;

    @Mock
    EmailService emailService;

    private LembreteScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new LembreteScheduler();
        scheduler.consultaRepository = consultaRepository;
        scheduler.emailService = emailService;
    }

    @Test
    @DisplayName("Deve enviar lembretes para paciente e profissional")
    void enviarLembretesComSucesso() {
        LocalDateTime agora = LocalDateTime.of(2026, 4, 18, 9, 0);
        Consulta consulta = criarConsulta(1L, agora.plusHours(3), "paciente@clinica.com", "psicologo@clinica.com");

        when(consultaRepository.buscarConsultasParaLembrete(eq(agora.plusHours(3)), eq(agora.plusHours(3).plusMinutes(3))))
                .thenReturn(Uni.createFrom().item(List.of(consulta)));
        when(emailService.mandarLembreConsulta(any(LembreteDeConsultaRequest.class)))
                .thenReturn(Uni.createFrom().voidItem());

        scheduler.enviarLembretes(agora).await().indefinitely();

        ArgumentCaptor<LembreteDeConsultaRequest> captor = ArgumentCaptor.forClass(LembreteDeConsultaRequest.class);
        verify(emailService, times(2)).mandarLembreConsulta(captor.capture());

        List<LembreteDeConsultaRequest> requests = captor.getAllValues();
        assertEquals(2, requests.size());
        assertTrue(requests.stream().anyMatch(r -> r.getDestinatario().equals("paciente@clinica.com")));
        assertTrue(requests.stream().anyMatch(r -> r.getDestinatario().equals("psicologo@clinica.com")));
        assertTrue(requests.stream().allMatch(r -> r.getNomePaciente().equals("Paciente Teste")));
        assertTrue(requests.stream().allMatch(r -> r.getNomeProfissional().equals("Profissional Teste")));
        assertTrue(requests.stream().allMatch(r -> r.getEspecialidade().equals(TipoUsuario.PSICOLOGO.name())));
        assertTrue(requests.stream().allMatch(r -> r.getDataConsulta().equals("18/04/2026")));
        assertTrue(requests.stream().allMatch(r -> r.getHoraConsulta().equals("12:00")));
    }

    @Test
    @DisplayName("Deve ignorar consulta de outro dia")
    void enviarLembretesIgnoraConsultaDeOutroDia() {
        LocalDateTime agora = LocalDateTime.of(2026, 4, 18, 22, 30);
        Consulta consulta = criarConsulta(2L, agora.plusHours(3), "paciente@clinica.com", "psicologo@clinica.com");

        when(consultaRepository.buscarConsultasParaLembrete(eq(agora.plusHours(3)), eq(agora.plusHours(3).plusMinutes(3))))
                .thenReturn(Uni.createFrom().item(List.of(consulta)));

        scheduler.enviarLembretes(agora).await().indefinitely();

        verify(emailService, never()).mandarLembreConsulta(any(LembreteDeConsultaRequest.class));
    }

    @Test
    @DisplayName("Deve enviar lembrete apenas para destinatario com email valido")
    void enviarLembretesSomenteParaEmailValido() {
        LocalDateTime agora = LocalDateTime.of(2026, 4, 18, 9, 0);
        Consulta consulta = criarConsulta(3L, agora.plusHours(3), "paciente@clinica.com", "   ");

        when(consultaRepository.buscarConsultasParaLembrete(eq(agora.plusHours(3)), eq(agora.plusHours(3).plusMinutes(3))))
                .thenReturn(Uni.createFrom().item(List.of(consulta)));
        when(emailService.mandarLembreConsulta(any(LembreteDeConsultaRequest.class)))
                .thenReturn(Uni.createFrom().voidItem());

        scheduler.enviarLembretes(agora).await().indefinitely();

        ArgumentCaptor<LembreteDeConsultaRequest> captor = ArgumentCaptor.forClass(LembreteDeConsultaRequest.class);
        verify(emailService, times(1)).mandarLembreConsulta(captor.capture());
        assertEquals("paciente@clinica.com", captor.getValue().getDestinatario());
    }

    @Test
    @DisplayName("Deve ignorar consulta sem paciente ou usuario")
    void enviarLembretesIgnoraConsultaSemRelacionamentos() {
        LocalDateTime agora = LocalDateTime.of(2026, 4, 18, 9, 0);
        Consulta consulta = new Consulta();
        consulta.setId(4L);
        consulta.setDataInicio(agora.plusHours(3));

        when(consultaRepository.buscarConsultasParaLembrete(eq(agora.plusHours(3)), eq(agora.plusHours(3).plusMinutes(3))))
                .thenReturn(Uni.createFrom().item(List.of(consulta)));

        scheduler.enviarLembretes(agora).await().indefinitely();

        verify(emailService, never()).mandarLembreConsulta(any(LembreteDeConsultaRequest.class));
    }

    @Test
    @DisplayName("Deve continuar o processamento quando um destinatario falhar")
    void enviarLembretesContinuaQuandoUmEnvioFalha() {
        LocalDateTime agora = LocalDateTime.of(2026, 4, 18, 9, 0);
        Consulta consulta = criarConsulta(5L, agora.plusHours(3), "paciente@clinica.com", "psicologo@clinica.com");

        when(consultaRepository.buscarConsultasParaLembrete(eq(agora.plusHours(3)), eq(agora.plusHours(3).plusMinutes(3))))
                .thenReturn(Uni.createFrom().item(List.of(consulta)));

        when(emailService.mandarLembreConsulta(any(LembreteDeConsultaRequest.class)))
                .thenAnswer(invocation -> {
                    LembreteDeConsultaRequest request = invocation.getArgument(0);
                    if ("paciente@clinica.com".equals(request.getDestinatario())) {
                        return Uni.createFrom().failure(new RuntimeException("Falha SMTP simulada"));
                    }
                    return Uni.createFrom().voidItem();
                });

        assertDoesNotThrow(() -> scheduler.enviarLembretes(agora).await().indefinitely());
        verify(emailService, times(2)).mandarLembreConsulta(any(LembreteDeConsultaRequest.class));
    }

    private Consulta criarConsulta(Long id, LocalDateTime dataInicio, String emailPaciente, String emailProfissional) {
        Paciente paciente = new Paciente();
        paciente.setId(10L);
        paciente.setNome("Paciente Teste");
        paciente.setEmail(emailPaciente);

        Usuario usuario = Usuario.builder()
                .nome("Profissional Teste")
                .email(emailProfissional)
                .senha("senha")
                .tipoUsuario(TipoUsuario.PSICOLOGO)
                .build();
        usuario.setId(20L);

        Consulta consulta = new Consulta();
        consulta.setId(id);
        consulta.setDataInicio(dataInicio);
        consulta.setPaciente(paciente);
        consulta.setUsuario(usuario);
        return consulta;
    }
}
