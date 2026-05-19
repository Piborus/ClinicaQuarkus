package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.FiliacaoRequest;
import br.ce.clinica.dto.response.FiliacaoResponse;
import br.ce.clinica.entity.Filiacao;
import br.ce.clinica.entity.Paciente;
import br.ce.clinica.enums.GrauParentesco;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.exception.UnprocessableEntityBusinessException;
import br.ce.clinica.repository.FiliacaoRepository;
import br.ce.clinica.repository.PacienteRepository;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("FiliacaoSeriveImpl Unit Tests")
class FiliacaoSeriveImplTest {

    @InjectMock
    FiliacaoRepository filiacaoRepository;

    @InjectMock
    PacienteRepository pacienteRepository;

    @Inject
    FiliacaoSeriveImpl filiacaoService;

    private Paciente paciente;
    private Filiacao filiacao;
    private FiliacaoRequest filiacaoRequest;

    @BeforeEach
    void setUp() {
        paciente = new Paciente();
        paciente.setId(1L);
        paciente.setNome("João da Silva");
        paciente.setStatus(true);

        filiacao = Filiacao.builder()
                .nome("Maria da Silva")
                .idade(45)
                .cpf("12345678901")
                .telefone("11999999999")
                .email("maria@email.com")
                .grauDeParentesco(GrauParentesco.GENITOR)
                .paciente(paciente)
                .build();
        filiacao.setId(1L);

        filiacaoRequest = FiliacaoRequest.builder()
                .nome("Maria da Silva Atualizada")
                .idade(46)
                .cpf("12345678901")
                .telefone("11888888888")
                .email("maria.atualizada@email.com")
                .grauDeParentesco(GrauParentesco.GENITOR)
                .build();
    }

    @Test
    @DisplayName("Deve buscar filiações por ID do paciente com sucesso")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findByPacienteIdComSucesso(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", 1L)).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
            when(filiacaoRepository.findByPacienteId(1L)).thenReturn(Uni.createFrom().item(List.of(filiacao)));
        });

        asserter.assertThat(
            () -> filiacaoService.findByPacienteId(1L),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals(filiacao.getNome(), result.get(0).getNome());
                assertEquals(filiacao.getCpf(), result.get(0).getCpf());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException quando paciente não existe ao buscar filiações")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findByPacienteIdPacienteNaoEncontrado(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", 1L)).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> filiacaoService.findByPacienteId(1L),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Paciente não encontrado.", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar UnprocessableEntityBusinessException quando paciente está inativo ao buscar filiações")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findByPacienteIdPacienteInativo(UniAsserter asserter) {
        asserter.execute(() -> {
            paciente.setStatus(false);
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", 1L)).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertFailedWith(
            () -> filiacaoService.findByPacienteId(1L),
            throwable -> {
                assertInstanceOf(UnprocessableEntityBusinessException.class, throwable);
                assertEquals("Paciente inativo, não é possível consultar as filiações.", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException quando não existem filiações para o paciente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findByPacienteIdFiliacoesNaoEncontradas(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", 1L)).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
            when(filiacaoRepository.findByPacienteId(1L)).thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> filiacaoService.findByPacienteId(1L),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Filiacões não encontradas para o paciente informado.", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar múltiplas filiações por ID do paciente com sucesso")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findByPacienteIdMultiplasFiliacoes(UniAsserter asserter) {
        Filiacao filiacao2 = Filiacao.builder()
                .nome("José da Silva")
                .idade(50)
                .cpf("98765432101")
                .telefone("11777777777")
                .email("jose@email.com")
                .grauDeParentesco(GrauParentesco.GENITOR)
                .paciente(paciente)
                .build();
        filiacao2.setId(2L);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", 1L)).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
            when(filiacaoRepository.findByPacienteId(1L)).thenReturn(Uni.createFrom().item(List.of(filiacao, filiacao2)));
        });

        asserter.assertThat(
            () -> filiacaoService.findByPacienteId(1L),
            result -> {
                assertNotNull(result);
                assertEquals(2, result.size());
            }
        );
    }

    @Test
    @DisplayName("Deve atualizar filiação com sucesso")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateFiliacaoComSucesso(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Filiacao> mockQuery = mock(PanacheQuery.class);
            when(filiacaoRepository.find("id", 1L)).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(filiacao));
        });

        asserter.assertThat(
            () -> filiacaoService.update(1L, filiacaoRequest),
            result -> {
                assertNotNull(result);
                assertEquals(filiacaoRequest.getNome(), result.getNome());
                assertEquals(filiacaoRequest.getIdade(), result.getIdade());
                assertEquals(filiacaoRequest.getCpf(), result.getCpf());
                assertEquals(filiacaoRequest.getTelefone(), result.getTelefone());
                assertEquals(filiacaoRequest.getEmail(), result.getEmail());
                assertEquals(filiacaoRequest.getGrauDeParentesco(), result.getGrauDeParentesco());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException quando filiação não existe ao atualizar")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateFiliacaoNaoEncontrada(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Filiacao> mockQuery = mock(PanacheQuery.class);
            when(filiacaoRepository.find("id", 1L)).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> filiacaoService.update(1L, filiacaoRequest),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Filiação não encontrada.", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar UnprocessableEntityBusinessException quando paciente está inativo ao atualizar filiação")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateFiliacaoPacienteInativo(UniAsserter asserter) {
        asserter.execute(() -> {
            paciente.setStatus(false);
            filiacao.setPaciente(paciente);
            PanacheQuery<Filiacao> mockQuery = mock(PanacheQuery.class);
            when(filiacaoRepository.find("id", 1L)).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(filiacao));
        });

        asserter.assertFailedWith(
            () -> filiacaoService.update(1L, filiacaoRequest),
            throwable -> {
                assertInstanceOf(UnprocessableEntityBusinessException.class, throwable);
                assertEquals("Paciente inativo, não é possível atualizar as filiações.", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve atualizar apenas campos não nulos da filiação")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateFiliacaoComCamposParciais(UniAsserter asserter) {
        FiliacaoRequest requestParcial = FiliacaoRequest.builder()
                .nome("Nome Atualizado")
                .cpf(null)
                .telefone(null)
                .email(null)
                .idade(null)
                .grauDeParentesco(null)
                .build();

        asserter.execute(() -> {
            PanacheQuery<Filiacao> mockQuery = mock(PanacheQuery.class);
            when(filiacaoRepository.find("id", 1L)).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(filiacao));
        });

        asserter.assertThat(
            () -> filiacaoService.update(1L, requestParcial),
            result -> {
                assertNotNull(result);
                assertEquals(requestParcial.getNome(), result.getNome());
            }
        );
    }

    @Test
    @DisplayName("Deve atualizar filiação com todos os campos preenchidos")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateFiliacaoComTodosCampos(UniAsserter asserter) {
        FiliacaoRequest requestCompleto = FiliacaoRequest.builder()
                .nome("Ana Maria")
                .idade(35)
                .cpf("11122233344")
                .telefone("11666666666")
                .email("ana.maria@email.com")
                .grauDeParentesco(GrauParentesco.IRMAO)
                .build();

        asserter.execute(() -> {
            PanacheQuery<Filiacao> mockQuery = mock(PanacheQuery.class);
            when(filiacaoRepository.find("id", 1L)).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(filiacao));
        });

        asserter.assertThat(
            () -> filiacaoService.update(1L, requestCompleto),
            result -> {
                assertNotNull(result);
                assertEquals(requestCompleto.getNome(), result.getNome());
                assertEquals(requestCompleto.getIdade(), result.getIdade());
                assertEquals(requestCompleto.getCpf(), result.getCpf());
                assertEquals(requestCompleto.getTelefone(), result.getTelefone());
                assertEquals(requestCompleto.getEmail(), result.getEmail());
                assertEquals(requestCompleto.getGrauDeParentesco(), result.getGrauDeParentesco());
            }
        );
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando paciente não possui filiações")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findByPacienteIdListaVazia(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", 1L)).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
            when(filiacaoRepository.findByPacienteId(1L)).thenReturn(Uni.createFrom().item(List.of()));
        });

        asserter.assertThat(
            () -> filiacaoService.findByPacienteId(1L),
            result -> {
                assertNotNull(result);
                assertTrue(result.isEmpty());
            }
        );
    }

    @Test
    @DisplayName("Deve converter corretamente Filiacao para FiliacaoResponse")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findByPacienteIdConversaoCorreta(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", 1L)).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
            when(filiacaoRepository.findByPacienteId(1L)).thenReturn(Uni.createFrom().item(List.of(filiacao)));
        });

        asserter.assertThat(
            () -> filiacaoService.findByPacienteId(1L),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.size());
                FiliacaoResponse response = result.get(0);
                assertEquals(filiacao.getId(), response.getId());
                assertEquals(filiacao.getNome(), response.getNome());
                assertEquals(filiacao.getIdade(), response.getIdade());
                assertEquals(filiacao.getCpf(), response.getCpf());
                assertEquals(filiacao.getTelefone(), response.getTelefone());
                assertEquals(filiacao.getEmail(), response.getEmail());
                assertEquals(filiacao.getGrauDeParentesco(), response.getGrauDeParentesco());
            }
        );
    }
}

