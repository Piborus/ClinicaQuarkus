package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.ProntuarioRequest;
import br.ce.clinica.entity.Paciente;
import br.ce.clinica.entity.Prontuario;
import br.ce.clinica.exception.BadRequestBusinessException;
import br.ce.clinica.exception.ConflictBusinessException;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.repository.PacienteRepository;
import br.ce.clinica.repository.ProntuarioRepository;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("ProntuarioServiceImpl Unit Tests")
class ProntuarioServiceImplTest {

    @InjectMock
    ProntuarioRepository prontuarioRepository;

    @InjectMock
    PacienteRepository pacienteRepository;

    @Inject
    ProntuarioServiceImpl prontuarioService;

    private Paciente paciente;
    private Prontuario prontuario;
    private ProntuarioRequest prontuarioRequest;

    @BeforeEach
    void setUp() {
        paciente = new Paciente();
        paciente.setId(1L);
        paciente.setNome("João da Silva");
        paciente.setStatus(true);

        prontuario = new Prontuario();
        prontuario.setId(1L);
        prontuario.setTexto("Prontuário do paciente");
        prontuario.setPaciente(paciente);

        prontuarioRequest = ProntuarioRequest.builder()
                .texto("Prontuário do paciente")
                .pacienteId(1L)
                .build();
    }

    @Test
    @DisplayName("Deve salvar prontuário com sucesso quando paciente está ativo")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void testSave_Success_ActivePatient(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", prontuarioRequest.getPacienteId()))
                    .thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
            when(prontuarioRepository.persist(any(Prontuario.class)))
                    .thenReturn(Uni.createFrom().item(prontuario));
        });

        asserter.assertThat(
            () -> prontuarioService.save(prontuarioRequest),
            result -> {
                assertNotNull(result);
                assertEquals(prontuario.getId(), result.getId());
                assertEquals(prontuario.getTexto(), result.getTexto());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException quando paciente não existe ao salvar")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void testSave_Fail_PatientNotFound(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", prontuarioRequest.getPacienteId()))
                    .thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> prontuarioService.save(prontuarioRequest),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Paciente nao encontrado", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar ConflictBusinessException quando paciente está inativo ao salvar")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void testSave_Fail_InactivePatient(UniAsserter asserter) {
        asserter.execute(() -> {
            paciente.setStatus(false);
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", prontuarioRequest.getPacienteId()))
                    .thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertFailedWith(
            () -> prontuarioService.save(prontuarioRequest),
            throwable -> {
                assertInstanceOf(ConflictBusinessException.class, throwable);
                assertEquals("Paciente inativo. Não é possível criar prontuario para paciente inativo.", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar prontuário por ID com sucesso")
    @RunOnVertxContext
    void testFindById_Success(UniAsserter asserter) {
        asserter.execute(() -> {
            when(prontuarioRepository.findById(1L))
                    .thenReturn(Uni.createFrom().item(prontuario));
        });

        asserter.assertThat(
            () -> prontuarioService.findById(1L),
            result -> {
                assertNotNull(result);
                assertEquals(prontuario.getId(), result.getId());
                assertEquals(prontuario.getTexto(), result.getTexto());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException quando prontuário não existe")
    @RunOnVertxContext
    void testFindById_Fail_NotFound(UniAsserter asserter) {
        asserter.execute(() -> {
            when(prontuarioRepository.findById(1L))
                    .thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> prontuarioService.findById(1L),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Prontuario nao encontrado", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar ConflictBusinessException quando paciente está inativo ao buscar")
    @RunOnVertxContext
    void testFindById_Fail_InactivePatient(UniAsserter asserter) {
        asserter.execute(() -> {
            paciente.setStatus(false);
            when(prontuarioRepository.findById(1L))
                    .thenReturn(Uni.createFrom().item(prontuario));
        });

        asserter.assertFailedWith(
            () -> prontuarioService.findById(1L),
            throwable -> {
                assertInstanceOf(ConflictBusinessException.class, throwable);
                assertEquals("Paciente inativo. Não é possível acessar prontuario de paciente inativo.", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve deletar prontuário por ID com sucesso")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void testDeleteById_Success(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Prontuario> mockQuery = mock(PanacheQuery.class);
            when(prontuarioRepository.find("id", 1L))
                    .thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(prontuario));
            when(prontuarioRepository.deleteById(1L)).thenReturn(Uni.createFrom().item(true));
        });

        asserter.assertThat(
            () -> prontuarioService.deleteById(1L),
            result -> assertTrue(result)
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException quando prontuário não existe ao deletar")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void testDeleteById_Fail_NotFound(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Prontuario> mockQuery = mock(PanacheQuery.class);
            when(prontuarioRepository.find("id", 1L))
                    .thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> prontuarioService.deleteById(1L),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Prontuario do paciente nao encontrado", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve atualizar prontuário com sucesso")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void testUpdate_Success(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Paciente> mockPacienteQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", prontuarioRequest.getPacienteId()))
                    .thenReturn(mockPacienteQuery);
            when(mockPacienteQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
            when(prontuarioRepository.findById(1L))
                    .thenReturn(Uni.createFrom().item(prontuario));
        });

        asserter.assertThat(
            () -> prontuarioService.update(1L, prontuarioRequest),
            result -> {
                assertNotNull(result);
                assertEquals(prontuario.getId(), result.getId());
                assertEquals(prontuarioRequest.getTexto(), result.getTexto());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException quando paciente não existe ao atualizar")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void testUpdate_Fail_PatientNotFound(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", prontuarioRequest.getPacienteId()))
                    .thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> prontuarioService.update(1L, prontuarioRequest),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Paciente nao encontrado", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar ConflictBusinessException quando paciente está inativo ao atualizar")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void testUpdate_Fail_InactivePatient(UniAsserter asserter) {
        asserter.execute(() -> {
            paciente.setStatus(false);
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", prontuarioRequest.getPacienteId()))
                    .thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertFailedWith(
            () -> prontuarioService.update(1L, prontuarioRequest),
            throwable -> {
                assertInstanceOf(ConflictBusinessException.class, throwable);
                assertEquals("Paciente inativo. Não é possível atualizar prontuario de paciente inativo.", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException quando prontuário não existe ao atualizar")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void testUpdate_Fail_ProntuarioNotFound(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", prontuarioRequest.getPacienteId()))
                    .thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
            when(prontuarioRepository.findById(1L))
                    .thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> prontuarioService.update(1L, prontuarioRequest),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Prontuario do paciente nao encontrado", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar prontuário por ID com paciente com sucesso")
    @RunOnVertxContext
    void testFindByIdWithPaciente_Success(UniAsserter asserter) {
        asserter.execute(() -> {
            when(prontuarioRepository.findByIdWithPaciente(1L))
                    .thenReturn(Uni.createFrom().item(prontuario));
        });

        asserter.assertThat(
            () -> prontuarioService.findByIdWithPaciente(1L),
            result -> {
                assertNotNull(result);
                assertEquals(prontuario.getId(), result.getId());
                assertEquals(prontuario.getTexto(), result.getTexto());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException quando prontuário não existe ao buscar com paciente")
    @RunOnVertxContext
    void testFindByIdWithPaciente_Fail_NotFound(UniAsserter asserter) {
        asserter.execute(() -> {
            when(prontuarioRepository.findByIdWithPaciente(1L))
                    .thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> prontuarioService.findByIdWithPaciente(1L),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Prontuario nao encontrado", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar prontuários paginados com sucesso sem sort")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void testFindPaginated_Success_WithoutSort(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        List<Prontuario> prontuarios = List.of(prontuario);

        asserter.execute(() -> {
            PanacheQuery<Prontuario> mockQuery = mock(PanacheQuery.class);
            when(prontuarioRepository.findPaginated(isNull(), isNull(), isNull()))
                    .thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(prontuarios));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> prontuarioService.findPaginated(page, null, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
                assertEquals(1L, result.getTotalCount());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar prontuários paginados com sucesso com sort ascendente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void testFindPaginated_Success_WithSortAsc(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "id,asc";
        List<Prontuario> prontuarios = List.of(prontuario);

        asserter.execute(() -> {
            PanacheQuery<Prontuario> mockQuery = mock(PanacheQuery.class);
            when(prontuarioRepository.findPaginated(any(), isNull(), isNull()))
                    .thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(prontuarios));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> prontuarioService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
                assertEquals(1L, result.getTotalCount());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar prontuários paginados com sucesso com sort descendente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void testFindPaginated_Success_WithSortDesc(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "texto,desc";
        List<Prontuario> prontuarios = List.of(prontuario);

        asserter.execute(() -> {
            PanacheQuery<Prontuario> mockQuery = mock(PanacheQuery.class);
            when(prontuarioRepository.findPaginated(any(), isNull(), isNull()))
                    .thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(prontuarios));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> prontuarioService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
                assertEquals(1L, result.getTotalCount());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar prontuários paginados com sucesso com filtros")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void testFindPaginated_Success_WithFilters(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        List<String> filterFields = List.of("texto");
        List<String> filterValues = List.of("prontuário");
        List<Prontuario> prontuarios = List.of(prontuario);

        asserter.execute(() -> {
            PanacheQuery<Prontuario> mockQuery = mock(PanacheQuery.class);
            when(prontuarioRepository.findPaginated(isNull(), eq(filterFields), eq(filterValues)))
                    .thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(prontuarios));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> prontuarioService.findPaginated(page, null, filterFields, filterValues),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
                assertEquals(1L, result.getTotalCount());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar BadRequestBusinessException quando campo de ordenação é inválido")
    @RunOnVertxContext
    void testFindPaginated_Fail_InvalidSortField(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "campoInvalido,asc";

        asserter.assertThat(
            () -> {
                BadRequestBusinessException exception = assertThrows(BadRequestBusinessException.class, () ->
                        prontuarioService.findPaginated(page, sort, null, null)
                );
                assertEquals("Campo de ordenacao invalido: campoInvalido", exception.getMessage());
                return Uni.createFrom().voidItem();
            },
            result -> {}
        );
    }

    @Test
    @DisplayName("Deve buscar prontuários paginados com sort apenas com campo (default asc)")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void testFindPaginated_Success_WithSortFieldOnly(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "id";
        List<Prontuario> prontuarios = List.of(prontuario);

        asserter.execute(() -> {
            PanacheQuery<Prontuario> mockQuery = mock(PanacheQuery.class);
            when(prontuarioRepository.findPaginated(any(), isNull(), isNull()))
                    .thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(prontuarios));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> prontuarioService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar prontuários paginados ignorando sort vazio")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void testFindPaginated_Success_WithEmptySort(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "";
        List<Prontuario> prontuarios = List.of(prontuario);

        asserter.execute(() -> {
            PanacheQuery<Prontuario> mockQuery = mock(PanacheQuery.class);
            when(prontuarioRepository.findPaginated(isNull(), isNull(), isNull()))
                    .thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(prontuarios));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> prontuarioService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar prontuários paginados ignorando sort com apenas espaços")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void testFindPaginated_Success_WithBlankSort(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "   ";
        List<Prontuario> prontuarios = List.of(prontuario);

        asserter.execute(() -> {
            PanacheQuery<Prontuario> mockQuery = mock(PanacheQuery.class);
            when(prontuarioRepository.findPaginated(isNull(), isNull(), isNull()))
                    .thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(prontuarios));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> prontuarioService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar prontuários paginados com sort e filtros combinados")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComSortEFiltros(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "id,asc";
        List<String> filterFields = List.of("texto");
        List<String> filterValues = List.of("prontuário");
        List<Prontuario> prontuarios = List.of(prontuario);

        asserter.execute(() -> {
            PanacheQuery<Prontuario> mockQuery = mock(PanacheQuery.class);
            when(prontuarioRepository.findPaginated(any(), eq(filterFields), eq(filterValues)))
                    .thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(prontuarios));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> prontuarioService.findPaginated(page, sort, filterFields, filterValues),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não existem prontuários")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedListaVazia(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        List<Prontuario> prontuariosVazio = List.of();

        asserter.execute(() -> {
            PanacheQuery<Prontuario> mockQuery = mock(PanacheQuery.class);
            when(prontuarioRepository.findPaginated(isNull(), isNull(), isNull()))
                    .thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(prontuariosVazio));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(0L));
        });

        asserter.assertThat(
            () -> prontuarioService.findPaginated(page, null, null, null),
            result -> {
                assertNotNull(result);
                assertTrue(result.getContent().isEmpty());
                assertEquals(0L, result.getTotalCount());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar prontuários na segunda página")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedSegundaPagina(UniAsserter asserter) {
        Page page = Page.of(1, 10);
        List<Prontuario> prontuarios = List.of(prontuario);

        asserter.execute(() -> {
            PanacheQuery<Prontuario> mockQuery = mock(PanacheQuery.class);
            when(prontuarioRepository.findPaginated(isNull(), isNull(), isNull()))
                    .thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(prontuarios));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(15L));
        });

        asserter.assertThat(
            () -> prontuarioService.findPaginated(page, null, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
                assertEquals(15L, result.getTotalCount());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar prontuários paginados com múltiplos resultados")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComMultiplosResultados(UniAsserter asserter) {
        Page page = Page.of(0, 10);

        Prontuario prontuario2 = new Prontuario();
        prontuario2.setId(2L);
        prontuario2.setTexto("Segundo prontuário");
        prontuario2.setPaciente(paciente);

        List<Prontuario> prontuarios = List.of(prontuario, prontuario2);

        asserter.execute(() -> {
            PanacheQuery<Prontuario> mockQuery = mock(PanacheQuery.class);
            when(prontuarioRepository.findPaginated(isNull(), isNull(), isNull()))
                    .thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(prontuarios));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(2L));
        });

        asserter.assertThat(
            () -> prontuarioService.findPaginated(page, null, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(2, result.getContent().size());
                assertEquals(2L, result.getTotalCount());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar prontuários paginados com tamanho de página diferente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComTamanhoPaginaDiferente(UniAsserter asserter) {
        Page page = Page.of(0, 5);
        List<Prontuario> prontuarios = List.of(prontuario);

        asserter.execute(() -> {
            PanacheQuery<Prontuario> mockQuery = mock(PanacheQuery.class);
            when(prontuarioRepository.findPaginated(isNull(), isNull(), isNull()))
                    .thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(prontuarios));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> prontuarioService.findPaginated(page, null, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar prontuários paginados com todos os campos de ordenação válidos")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComTodosCamposOrdenacaoValidos(UniAsserter asserter) {
        List<String> camposValidos = List.of("id", "texto");
        Page page = Page.of(0, 10);
        List<Prontuario> prontuarios = List.of(prontuario);

        asserter.execute(() -> {
            PanacheQuery<Prontuario> mockQuery = mock(PanacheQuery.class);
            when(prontuarioRepository.findPaginated(any(), isNull(), isNull()))
                    .thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(prontuarios));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        for (String campo : camposValidos) {
            asserter.assertThat(
                () -> prontuarioService.findPaginated(page, campo + ",asc", null, null),
                result -> assertNotNull(result)
            );
        }
    }

    @Test
    @DisplayName("Deve buscar prontuários paginados com múltiplos filtros")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComMultiplosFiltros(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        List<String> filterFields = List.of("texto", "id");
        List<String> filterValues = List.of("prontuário", "1");
        List<Prontuario> prontuarios = List.of(prontuario);

        asserter.execute(() -> {
            PanacheQuery<Prontuario> mockQuery = mock(PanacheQuery.class);
            when(prontuarioRepository.findPaginated(isNull(), eq(filterFields), eq(filterValues)))
                    .thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(prontuarios));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> prontuarioService.findPaginated(page, null, filterFields, filterValues),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve salvar prontuário com texto longo")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void saveProntuarioComTextoLongo(UniAsserter asserter) {
        String textoLongo = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. ".repeat(100);
        prontuarioRequest.setTexto(textoLongo);
        prontuario.setTexto(textoLongo);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", prontuarioRequest.getPacienteId()))
                    .thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
            when(prontuarioRepository.persist(any(Prontuario.class)))
                    .thenReturn(Uni.createFrom().item(prontuario));
        });

        asserter.assertThat(
            () -> prontuarioService.save(prontuarioRequest),
            result -> {
                assertNotNull(result);
                assertEquals(textoLongo, result.getTexto());
            }
        );
    }

    @Test
    @DisplayName("Deve atualizar prontuário com texto alterado")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateProntuarioComTextoAlterado(UniAsserter asserter) {
        String novoTexto = "Texto atualizado do prontuário";
        ProntuarioRequest updateRequest = ProntuarioRequest.builder()
                .texto(novoTexto)
                .pacienteId(1L)
                .build();

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockPacienteQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", updateRequest.getPacienteId()))
                    .thenReturn(mockPacienteQuery);
            when(mockPacienteQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
            when(prontuarioRepository.findById(1L))
                    .thenReturn(Uni.createFrom().item(prontuario));
        });

        asserter.assertThat(
            () -> prontuarioService.update(1L, updateRequest),
            result -> {
                assertNotNull(result);
                assertEquals(novoTexto, result.getTexto());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar prontuário por ID com paciente ativo")
    @RunOnVertxContext
    void findByIdComPacienteAtivo(UniAsserter asserter) {
        paciente.setStatus(true);
        prontuario.setPaciente(paciente);

        asserter.execute(() -> {
            when(prontuarioRepository.findById(1L))
                    .thenReturn(Uni.createFrom().item(prontuario));
        });

        asserter.assertThat(
            () -> prontuarioService.findById(1L),
            result -> {
                assertNotNull(result);
                assertEquals(prontuario.getId(), result.getId());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar prontuário com paciente associado")
    @RunOnVertxContext
    void findByIdWithPacienteComPacienteAssociado(UniAsserter asserter) {
        asserter.execute(() -> {
            when(prontuarioRepository.findByIdWithPaciente(1L))
                    .thenReturn(Uni.createFrom().item(prontuario));
        });

        asserter.assertThat(
            () -> prontuarioService.findByIdWithPaciente(1L),
            result -> {
                assertNotNull(result);
                assertEquals(prontuario.getId(), result.getId());
                assertEquals(prontuario.getTexto(), result.getTexto());
            }
        );
    }

    @Test
    @DisplayName("Deve deletar prontuário existente retornando true")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void deleteByIdRetornaTrue(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Prontuario> mockQuery = mock(PanacheQuery.class);
            when(prontuarioRepository.find("id", 1L))
                    .thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(prontuario));
            when(prontuarioRepository.deleteById(1L)).thenReturn(Uni.createFrom().item(true));
        });

        asserter.assertThat(
            () -> prontuarioService.deleteById(1L),
            result -> {
                assertNotNull(result);
                assertTrue(result);
            }
        );
    }
}
