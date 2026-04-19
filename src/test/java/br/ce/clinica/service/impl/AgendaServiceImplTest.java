package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.AgendaRequest;
import br.ce.clinica.dto.request.IntervaloConsultaRequest;
import br.ce.clinica.entity.Consulta;
import br.ce.clinica.entity.Paciente;
import br.ce.clinica.entity.Usuario;
import br.ce.clinica.enums.StatusConfirmacao;
import br.ce.clinica.enums.StatusConsulta;
import br.ce.clinica.enums.TipoUsuario;
import br.ce.clinica.exception.ConflictBusinessException;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.exception.UnprocessableEntityBusinessException;
import br.ce.clinica.repository.ConsultaRepository;
import br.ce.clinica.repository.PacienteRepository;
import br.ce.clinica.repository.UsuarioRepository;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgendaServiceImpl Unit Tests")
class AgendaServiceImplTest {

    @Mock
    ConsultaRepository consultaRepository;

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    PacienteRepository pacienteRepository;

    @InjectMocks
    AgendaServiceImpl agendaService;

    private MockedStatic<Panache> panacheMock;
    private Paciente paciente;
    private Usuario usuario;
    private Consulta consulta;
    private AgendaRequest agendaRequest;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        panacheMock = mockStatic(Panache.class);
        panacheMock.when(() -> Panache.withTransaction(any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<Uni<?>>) invocation.getArgument(0)).get());

        paciente = new Paciente();
        paciente.setId(1L);
        paciente.setNome("Paciente Teste");
        paciente.setCpf("12345678901");
        paciente.setResponsaveis(new HashSet<>());
        paciente.setTransacao(new HashSet<>());
        paciente.setProntuarioDoPaciente(new HashSet<>());

        usuario = Usuario.builder()
                .nome("Profissional Teste")
                .email("profissional@clinica.com")
                .senha("senha")
                .tipoUsuario(TipoUsuario.PSICOLOGO)
                .build();
        usuario.setId(2L);

        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime fim = inicio.plusHours(1);

        consulta = Consulta.builder()
                .dataInicio(inicio)
                .dataFim(fim)
                .statusConsulta(StatusConsulta.AGENDADA)
                .statusConfirmacao(StatusConfirmacao.PENDENTE)
                .paciente(paciente)
                .usuario(usuario)
                .build();
        consulta.setId(10L);

        agendaRequest = AgendaRequest.builder()
                .idpaciente(paciente.getId())
                .idUsuario(usuario.getId())
                .horario(inicio)
                .build();
    }

    @AfterEach
    void tearDown() {
        panacheMock.close();
    }

    @Test
    @DisplayName("Deve agendar consulta com sucesso")
    void scheduleConsultationComSucesso() {
        when(pacienteRepository.findByIdWithCollections(agendaRequest.getIdpaciente()))
                .thenReturn(Uni.createFrom().item(paciente));
        when(usuarioRepository.findById(agendaRequest.getIdUsuario()))
                .thenReturn(Uni.createFrom().item(usuario));
        when(consultaRepository.existeConflitoHorario(
                eq(usuario.getId()),
                eq(agendaRequest.getHorario()),
                eq(agendaRequest.getHorario().plusHours(1))))
                .thenReturn(Uni.createFrom().item(false));
        when(consultaRepository.persist(any(Consulta.class)))
                .thenAnswer(invocation -> {
                    Consulta persisted = invocation.getArgument(0);
                    persisted.setId(consulta.getId());
                    return Uni.createFrom().item(persisted);
                });

        var result = agendaService.scheduleConsultation(agendaRequest).await().indefinitely();

        assertNotNull(result);
        assertEquals(consulta.getId(), result.getId());
        assertEquals(StatusConsulta.AGENDADA, result.getStatusConsulta());
        assertEquals(StatusConfirmacao.PENDENTE, result.getStatusConfirmacao());
        assertEquals(paciente.getId(), result.getPaciente().getId());
        assertEquals(usuario.getId(), result.getUsuario().getId());
    }

    @Test
    @DisplayName("Deve falhar ao agendar consulta no passado")
    void scheduleConsultationFalhaQuandoDataNoPassado() {
        agendaRequest.setHorario(LocalDateTime.now().minusHours(1));

        var throwable = assertThrows(UnprocessableEntityBusinessException.class,
                () -> agendaService.scheduleConsultation(agendaRequest).await().indefinitely());

        assertEquals("A data e hora da consulta não podem ser no passado.", throwable.getMessage());
    }

    @Test
    @DisplayName("Deve falhar ao agendar consulta quando paciente nao existe")
    void scheduleConsultationFalhaQuandoPacienteNaoExiste() {
        when(pacienteRepository.findByIdWithCollections(agendaRequest.getIdpaciente()))
                .thenReturn(Uni.createFrom().nullItem());

        var throwable = assertThrows(NotFoundBusinessException.class,
                () -> agendaService.scheduleConsultation(agendaRequest).await().indefinitely());

        assertEquals("Paciente não encontrado.", throwable.getMessage());
    }

    @Test
    @DisplayName("Deve falhar ao agendar consulta quando usuario nao existe")
    void scheduleConsultationFalhaQuandoUsuarioNaoExiste() {
        when(pacienteRepository.findByIdWithCollections(agendaRequest.getIdpaciente()))
                .thenReturn(Uni.createFrom().item(paciente));
        when(usuarioRepository.findById(agendaRequest.getIdUsuario()))
                .thenReturn(Uni.createFrom().nullItem());

        var throwable = assertThrows(NotFoundBusinessException.class,
                () -> agendaService.scheduleConsultation(agendaRequest).await().indefinitely());

        assertEquals("Usuário não encontrado.", throwable.getMessage());
    }

    @Test
    @DisplayName("Deve falhar ao agendar consulta quando horario esta ocupado")
    void scheduleConsultationFalhaQuandoHorarioOcupado() {
        when(pacienteRepository.findByIdWithCollections(agendaRequest.getIdpaciente()))
                .thenReturn(Uni.createFrom().item(paciente));
        when(usuarioRepository.findById(agendaRequest.getIdUsuario()))
                .thenReturn(Uni.createFrom().item(usuario));
        when(consultaRepository.existeConflitoHorario(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Uni.createFrom().item(true));

        var throwable = assertThrows(ConflictBusinessException.class,
                () -> agendaService.scheduleConsultation(agendaRequest).await().indefinitely());

        assertEquals("O horário selecionado já está ocupado.", throwable.getMessage());
    }

    @Test
    @DisplayName("Deve cancelar consulta com sucesso")
    void cancelConsultationComSucesso() {
        when(consultaRepository.findById(consulta.getId()))
                .thenReturn(Uni.createFrom().item(consulta));

        var result = agendaService.cancelConsultation(consulta.getId()).await().indefinitely();

        assertNull(result);
        assertEquals(StatusConsulta.CANCELADA, consulta.getStatusConsulta());
        assertEquals(StatusConfirmacao.RECUSADA, consulta.getStatusConfirmacao());
    }

    @Test
    @DisplayName("Deve falhar ao cancelar consulta inexistente")
    void cancelConsultationFalhaQuandoConsultaNaoExiste() {
        when(consultaRepository.findById(consulta.getId()))
                .thenReturn(Uni.createFrom().nullItem());

        var throwable = assertThrows(NotFoundBusinessException.class,
                () -> agendaService.cancelConsultation(consulta.getId()).await().indefinitely());

        assertEquals("Consulta não encontrada.", throwable.getMessage());
    }

    @Test
    @DisplayName("Deve retornar horarios disponiveis desconsiderando conflitos e almoco")
    void findAvailableTimesComSucesso() {
        LocalDate data = LocalDate.now().plusDays(1);
        List<IntervaloConsultaRequest> ocupados = List.of(
                IntervaloConsultaRequest.builder().dataInicio(data.atTime(9, 0)).dataFim(data.atTime(10, 0)).build(),
                IntervaloConsultaRequest.builder().dataInicio(data.atTime(15, 0)).dataFim(data.atTime(16, 0)).build()
        );

        when(consultaRepository.buscarHorariosOcupadosDoDia(usuario.getId(), data.atTime(8, 0), data.atTime(22, 0)))
                .thenReturn(Uni.createFrom().item(ocupados));

        var result = agendaService.findAvailableTimes(usuario.getId(), data).await().indefinitely();

        assertNotNull(result);
        assertFalse(result.contains(LocalTime.of(9, 0)));
        assertFalse(result.contains(LocalTime.of(12, 0)));
        assertFalse(result.contains(LocalTime.of(15, 0)));
        assertTrue(result.contains(LocalTime.of(8, 0)));
        assertTrue(result.contains(LocalTime.of(10, 0)));
        assertTrue(result.contains(LocalTime.of(21, 0)));
        assertEquals(11, result.size());
    }

    @Test
    @DisplayName("Deve buscar consulta por id com sucesso")
    void findByIdComSucesso() {
        when(consultaRepository.findById(consulta.getId()))
                .thenReturn(Uni.createFrom().item(consulta));

        var result = agendaService.findById(consulta.getId()).await().indefinitely();

        assertNotNull(result);
        assertEquals(consulta.getId(), result.getId());
        assertEquals(consulta.getDataInicio(), result.getDataInicio());
    }

    @Test
    @DisplayName("Deve falhar ao buscar consulta inexistente por id")
    void findByIdFalhaQuandoConsultaNaoExiste() {
        when(consultaRepository.findById(consulta.getId()))
                .thenReturn(Uni.createFrom().nullItem());

        var throwable = assertThrows(NotFoundBusinessException.class,
                () -> agendaService.findById(consulta.getId()).await().indefinitely());

        assertEquals("Consulta não encontrada.", throwable.getMessage());
    }
}
