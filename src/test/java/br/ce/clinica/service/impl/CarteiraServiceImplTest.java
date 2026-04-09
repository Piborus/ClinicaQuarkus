package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.CarteiraRequest;
import br.ce.clinica.entity.Carteira;
import br.ce.clinica.entity.Paciente;
import br.ce.clinica.enums.TipoDePagamento;
import br.ce.clinica.enums.TipoMovimento;
import br.ce.clinica.exception.BadRequestBusinessException;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.exception.UnprocessableEntityBusinessException;
import br.ce.clinica.repository.CarteiraRepository;
import br.ce.clinica.repository.PacienteRepository;
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

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("CarteiraServiceImpl Unit Tests")
class CarteiraServiceImplTest {

    @InjectMock
    CarteiraRepository carteiraRepository;

    @InjectMock
    PacienteRepository pacienteRepository;

    @Inject
    CarteiraServiceImpl carteiraService;

    private Paciente paciente;

    private Carteira carteira;

    private CarteiraRequest carteiraRequest;

    @BeforeEach
    void setUp() {
        paciente = new Paciente();
        paciente.setId(1L);
        paciente.setNome("João da Silva");
        paciente.setStatus(true);

        carteira = new Carteira();
        carteira.setId(1L);
        carteira.setValor(new BigDecimal("150.75"));
        carteira.setDescricao("Pagamento de consulta médica");
        carteira.setTipoMovimento(TipoMovimento.ENTRADA);
        carteira.setTipoDePagamento(TipoDePagamento.PIX);
        carteira.setPaciente(paciente);

        carteiraRequest = CarteiraRequest.builder()
                .valor(new BigDecimal("150.75"))
                .descricao("Pagamento de consulta médica")
                .tipoMovimento(TipoMovimento.ENTRADA)
                .tipoDePagamento(TipoDePagamento.PIX)
                .pacienteId(1L)
                .build();
    }

    @Test
    @DisplayName("Deve salvar transação com sucesso quando paciente está ativo")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void saveTransacaoComSucesso(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", carteiraRequest.getPacienteId())).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
            when(carteiraRepository.persist(any(Carteira.class))).thenReturn(Uni.createFrom().item(carteira));
        });

        asserter.assertThat(
            () -> carteiraService.save(carteiraRequest),
            result -> {
                assertNotNull(result);
                assertEquals(carteira.getId(), result.getId());
                assertEquals(carteira.getValor(), result.getValor());
                assertEquals(carteira.getDescricao(), result.getDescricao());
                assertEquals(carteira.getTipoMovimento(), result.getTipoMovimento());
                assertEquals(carteira.getTipoDePagamento(), result.getTipoDePagamento());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException quando paciente não existe ao salvar")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void saveFalhaPacienteNaoEncontrado(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", carteiraRequest.getPacienteId())).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> carteiraService.save(carteiraRequest),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Paciente não encontrado", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar UnprocessableEntityBusinessException quando paciente está inativo ao salvar")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void saveFalhaPacienteInativo(UniAsserter asserter) {
        asserter.execute(() -> {
            paciente.setStatus(false);
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", carteiraRequest.getPacienteId())).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertFailedWith(
            () -> carteiraService.save(carteiraRequest),
            throwable -> {
                assertInstanceOf(UnprocessableEntityBusinessException.class, throwable);
                assertEquals("Paciente inativo, não é possível realizar transações", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar transação por ID com sucesso")
    @RunOnVertxContext
    void findByIdComSucesso(UniAsserter asserter) {
        asserter.execute(() -> {
            when(carteiraRepository.findByIdWithPaciente(1L)).thenReturn(Uni.createFrom().item(carteira));
        });

        asserter.assertThat(
            () -> carteiraService.findById(1L),
            result -> {
                assertNotNull(result);
                assertEquals(carteira.getId(), result.getId());
                assertEquals(carteira.getValor(), result.getValor());
                assertEquals(carteira.getDescricao(), result.getDescricao());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException quando transação não existe ao buscar por ID")
    @RunOnVertxContext
    void findByIdTransacaoNaoEncontrada(UniAsserter asserter) {
        asserter.execute(() -> {
            when(carteiraRepository.findByIdWithPaciente(1L)).thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> carteiraService.findById(1L),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Transação não encontrada", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve atualizar transação com sucesso")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateTransacaoComSucesso(UniAsserter asserter) {
        CarteiraRequest updateRequest = CarteiraRequest.builder()
                .valor(new BigDecimal("200.00"))
                .descricao("Pagamento atualizado")
                .tipoMovimento(TipoMovimento.SAIDA)
                .tipoDePagamento(TipoDePagamento.CREDITO)
                .pacienteId(1L)
                .build();

        asserter.execute(() -> {
            PanacheQuery<Carteira> mockQueryCarteira = mock(PanacheQuery.class);
            when(carteiraRepository.find("id", 1L)).thenReturn(mockQueryCarteira);
            when(mockQueryCarteira.firstResult()).thenReturn(Uni.createFrom().item(carteira));
            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> carteiraService.update(1L, updateRequest),
            result -> {
                assertNotNull(result);
                assertEquals(updateRequest.getDescricao(), result.getDescricao());
                assertEquals(updateRequest.getValor(), result.getValor());
                assertEquals(updateRequest.getTipoMovimento(), result.getTipoMovimento());
                assertEquals(updateRequest.getTipoDePagamento(), result.getTipoDePagamento());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException quando transação não existe ao atualizar")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateFalhaTransacaoNaoEncontrada(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Carteira> mockQueryCarteira = mock(PanacheQuery.class);
            when(carteiraRepository.find("id", 1L)).thenReturn(mockQueryCarteira);
            when(mockQueryCarteira.firstResult()).thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> carteiraService.update(1L, carteiraRequest),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Transação não encontrada", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException quando paciente não existe ao atualizar")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateFalhaPacienteNaoEncontrado(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Carteira> mockQueryCarteira = mock(PanacheQuery.class);
            when(carteiraRepository.find("id", 1L)).thenReturn(mockQueryCarteira);
            when(mockQueryCarteira.firstResult()).thenReturn(Uni.createFrom().item(carteira));
            when(pacienteRepository.findById(carteiraRequest.getPacienteId())).thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> carteiraService.update(1L, carteiraRequest),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Paciente não encontrado", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar UnprocessableEntityBusinessException quando paciente está inativo ao atualizar")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateFalhaPacienteInativo(UniAsserter asserter) {
        asserter.execute(() -> {
            paciente.setStatus(false);
            PanacheQuery<Carteira> mockQueryCarteira = mock(PanacheQuery.class);
            when(carteiraRepository.find("id", 1L)).thenReturn(mockQueryCarteira);
            when(mockQueryCarteira.firstResult()).thenReturn(Uni.createFrom().item(carteira));
            when(pacienteRepository.findById(carteiraRequest.getPacienteId())).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertFailedWith(
            () -> carteiraService.update(1L, carteiraRequest),
            throwable -> {
                assertInstanceOf(UnprocessableEntityBusinessException.class, throwable);
                assertEquals("Paciente inativo, não é possível realizar transações", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar transações paginadas com sucesso sem sort")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedSemSort(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        List<Carteira> carteiras = List.of(carteira);

        asserter.execute(() -> {
            PanacheQuery<Carteira> mockQuery = mock(PanacheQuery.class);
            when(carteiraRepository.findPaginated(isNull(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(carteiras));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> carteiraService.findPaginated(page, null, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
                assertEquals(1L, result.getTotalCount());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar transações paginadas com sort ascendente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComSortAsc(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "valor,asc";
        List<Carteira> carteiras = List.of(carteira);

        asserter.execute(() -> {
            PanacheQuery<Carteira> mockQuery = mock(PanacheQuery.class);
            when(carteiraRepository.findPaginated(any(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(carteiras));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> carteiraService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar transações paginadas com sort descendente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComSortDesc(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "valor,desc";
        List<Carteira> carteiras = List.of(carteira);

        asserter.execute(() -> {
            PanacheQuery<Carteira> mockQuery = mock(PanacheQuery.class);
            when(carteiraRepository.findPaginated(any(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(carteiras));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> carteiraService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar transações paginadas com filtros")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComFiltros(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        List<String> filterFields = List.of("descricao");
        List<String> filterValues = List.of("Pagamento");
        List<Carteira> carteiras = List.of(carteira);

        asserter.execute(() -> {
            PanacheQuery<Carteira> mockQuery = mock(PanacheQuery.class);
            when(carteiraRepository.findPaginated(isNull(), eq(filterFields), eq(filterValues))).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(carteiras));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> carteiraService.findPaginated(page, null, filterFields, filterValues),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar BadRequestBusinessException para campo de ordenação inválido")
    @RunOnVertxContext
    void findPaginatedCampoOrdenacaoInvalido(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "campoInvalido,asc";

        asserter.assertThat(
            () -> {
                BadRequestBusinessException exception = assertThrows(BadRequestBusinessException.class, () ->
                        carteiraService.findPaginated(page, sort, null, null)
                );
                assertEquals("Campo de ordenação invalido: campoInvalido", exception.getMessage());
                return Uni.createFrom().voidItem();
            },
            result -> {}
        );
    }

    @Test
    @DisplayName("Deve buscar transações paginadas com sort apenas campo (default asc)")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComSortApenasCampo(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "id";
        List<Carteira> carteiras = List.of(carteira);

        asserter.execute(() -> {
            PanacheQuery<Carteira> mockQuery = mock(PanacheQuery.class);
            when(carteiraRepository.findPaginated(any(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(carteiras));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> carteiraService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar transações paginadas ignorando sort vazio")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComSortVazio(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "";
        List<Carteira> carteiras = List.of(carteira);

        asserter.execute(() -> {
            PanacheQuery<Carteira> mockQuery = mock(PanacheQuery.class);
            when(carteiraRepository.findPaginated(isNull(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(carteiras));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> carteiraService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar transações paginadas ignorando sort com espaços")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComSortEspacos(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "   ";
        List<Carteira> carteiras = List.of(carteira);

        asserter.execute(() -> {
            PanacheQuery<Carteira> mockQuery = mock(PanacheQuery.class);
            when(carteiraRepository.findPaginated(isNull(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(carteiras));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> carteiraService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar transações paginadas com todos os campos de ordenação válidos")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComTodosCamposOrdenacaoValidos(UniAsserter asserter) {
        List<String> camposValidos = List.of("id", "valor", "descricao", "tipoMovimento", "tipoDePagamento");
        Page page = Page.of(0, 10);
        List<Carteira> carteiras = List.of(carteira);

        asserter.execute(() -> {
            PanacheQuery<Carteira> mockQuery = mock(PanacheQuery.class);
            when(carteiraRepository.findPaginated(any(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(carteiras));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        for (String campo : camposValidos) {
            asserter.assertThat(
                () -> carteiraService.findPaginated(page, campo + ",asc", null, null),
                result -> assertNotNull(result)
            );
        }
    }

    @Test
    @DisplayName("Deve salvar transação com tipo movimento ENTRADA")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void saveTransacaoComTipoMovimentoEntrada(UniAsserter asserter) {
        carteiraRequest.setTipoMovimento(TipoMovimento.ENTRADA);
        carteira.setTipoMovimento(TipoMovimento.ENTRADA);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", carteiraRequest.getPacienteId())).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
            when(carteiraRepository.persist(any(Carteira.class))).thenReturn(Uni.createFrom().item(carteira));
        });

        asserter.assertThat(
            () -> carteiraService.save(carteiraRequest),
            result -> {
                assertNotNull(result);
                assertEquals(TipoMovimento.ENTRADA, result.getTipoMovimento());
            }
        );
    }

    @Test
    @DisplayName("Deve salvar transação com tipo movimento SAIDA")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void saveTransacaoComTipoMovimentoSaida(UniAsserter asserter) {
        carteiraRequest.setTipoMovimento(TipoMovimento.SAIDA);
        carteira.setTipoMovimento(TipoMovimento.SAIDA);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", carteiraRequest.getPacienteId())).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
            when(carteiraRepository.persist(any(Carteira.class))).thenReturn(Uni.createFrom().item(carteira));
        });

        asserter.assertThat(
            () -> carteiraService.save(carteiraRequest),
            result -> {
                assertNotNull(result);
                assertEquals(TipoMovimento.SAIDA, result.getTipoMovimento());
            }
        );
    }

    @Test
    @DisplayName("Deve salvar transação com tipo de pagamento CREDITO")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void saveTransacaoComTipoPagamentoCredito(UniAsserter asserter) {
        carteiraRequest.setTipoDePagamento(TipoDePagamento.CREDITO);
        carteira.setTipoDePagamento(TipoDePagamento.CREDITO);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", carteiraRequest.getPacienteId())).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
            when(carteiraRepository.persist(any(Carteira.class))).thenReturn(Uni.createFrom().item(carteira));
        });

        asserter.assertThat(
            () -> carteiraService.save(carteiraRequest),
            result -> {
                assertNotNull(result);
                assertEquals(TipoDePagamento.CREDITO, result.getTipoDePagamento());
            }
        );
    }

    @Test
    @DisplayName("Deve salvar transação com tipo de pagamento DEBITO")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void saveTransacaoComTipoPagamentoDebito(UniAsserter asserter) {
        carteiraRequest.setTipoDePagamento(TipoDePagamento.DEBITO);
        carteira.setTipoDePagamento(TipoDePagamento.DEBITO);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", carteiraRequest.getPacienteId())).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
            when(carteiraRepository.persist(any(Carteira.class))).thenReturn(Uni.createFrom().item(carteira));
        });

        asserter.assertThat(
            () -> carteiraService.save(carteiraRequest),
            result -> {
                assertNotNull(result);
                assertEquals(TipoDePagamento.DEBITO, result.getTipoDePagamento());
            }
        );
    }

    @Test
    @DisplayName("Deve salvar transação com tipo de pagamento DINHEIRO")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void saveTransacaoComTipoPagamentoDinheiro(UniAsserter asserter) {
        carteiraRequest.setTipoDePagamento(TipoDePagamento.DINHEIRO);
        carteira.setTipoDePagamento(TipoDePagamento.DINHEIRO);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", carteiraRequest.getPacienteId())).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
            when(carteiraRepository.persist(any(Carteira.class))).thenReturn(Uni.createFrom().item(carteira));
        });

        asserter.assertThat(
            () -> carteiraService.save(carteiraRequest),
            result -> {
                assertNotNull(result);
                assertEquals(TipoDePagamento.DINHEIRO, result.getTipoDePagamento());
            }
        );
    }

    @Test
    @DisplayName("Deve salvar transação com tipo de pagamento PIX")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void saveTransacaoComTipoPagamentoPix(UniAsserter asserter) {
        carteiraRequest.setTipoDePagamento(TipoDePagamento.PIX);
        carteira.setTipoDePagamento(TipoDePagamento.PIX);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.find("id", carteiraRequest.getPacienteId())).thenReturn(mockQuery);
            when(mockQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
            when(carteiraRepository.persist(any(Carteira.class))).thenReturn(Uni.createFrom().item(carteira));
        });

        asserter.assertThat(
            () -> carteiraService.save(carteiraRequest),
            result -> {
                assertNotNull(result);
                assertEquals(TipoDePagamento.PIX, result.getTipoDePagamento());
            }
        );
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não existem transações")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedListaVazia(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        List<Carteira> carteirasVazia = List.of();

        asserter.execute(() -> {
            PanacheQuery<Carteira> mockQuery = mock(PanacheQuery.class);
            when(carteiraRepository.findPaginated(isNull(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(carteirasVazia));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(0L));
        });

        asserter.assertThat(
            () -> carteiraService.findPaginated(page, null, null, null),
            result -> {
                assertNotNull(result);
                assertTrue(result.getContent().isEmpty());
                assertEquals(0L, result.getTotalCount());
            }
        );
    }
}

