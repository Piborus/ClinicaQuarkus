package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.EnderecoRequest;
import br.ce.clinica.dto.request.FiliacaoRequest;
import br.ce.clinica.dto.request.PacienteRequest;
import br.ce.clinica.entity.Endereco;
import br.ce.clinica.entity.Filiacao;
import br.ce.clinica.entity.Paciente;
import br.ce.clinica.enums.Sexo;
import br.ce.clinica.exception.BadRequestBusinessException;
import br.ce.clinica.exception.ConflictBusinessException;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.exception.UnprocessableEntityBusinessException;
import br.ce.clinica.repository.*;
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

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("PacienteServiceImpl Unit Tests")
class PacienteServiceImplTest {

    @InjectMock
    PacienteRepository pacienteRepository;

    @InjectMock
    CarteiraRepository carteiraRepository;

    @InjectMock
    ProntuarioRepository prontuarioRepository;

    @InjectMock
    FiliacaoRepository filiacaoRepository;

    @InjectMock
    AnamneseRepository anamneseRepository;

    @Inject
    PacienteServiceImpl pacienteService;

    private Paciente paciente;
    private PacienteRequest pacienteRequest;

    @BeforeEach
    void setUp() {
        paciente = new Paciente();
        paciente.setId(1L);
        paciente.setNome("João da Silva");
        paciente.setCpf("12345678901");
        paciente.setRg("123456789");
        paciente.setDataNascimento(LocalDate.of(1990, 1, 1));
        paciente.setSexo(Sexo.MASCULINO);
        paciente.setTelefone("11999999999");
        paciente.setEmail("joao@email.com");
        paciente.setIdade(34);
        paciente.setStatus(true);
        paciente.setResponsaveis(new HashSet<>());
        paciente.setTransacao(new HashSet<>());
        paciente.setProntuarioDoPaciente(new HashSet<>());

        pacienteRequest = PacienteRequest.builder()
                .nome("João da Silva")
                .cpf("12345678901")
                .rg("123456789")
                .dataNascimento(LocalDate.of(1990, 1, 1))
                .sexo(Sexo.MASCULINO)
                .telefone("11999999999")
                .email("joao@email.com")
                .idade(34)
                .build();
    }

    @Test
    @DisplayName("Deve salvar paciente com sucesso")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void savePacienteComSucesso(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);

            when(pacienteRepository.find("cpf", pacienteRequest.getCpf())).thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg", pacienteRequest.getRg())).thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.persist(any(Paciente.class))).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> pacienteService.save(pacienteRequest),
            result -> {
                assertNotNull(result);
                assertEquals(paciente.getId(), result.getId());
                assertEquals(paciente.getNome(), result.getNome());
            }
        );
    }

    @Test
    @DisplayName("Deve salvar paciente com endereco")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void savePacienteComEndereco(UniAsserter asserter) {
        EnderecoRequest enderecoRequest = EnderecoRequest.builder()
                .logradouro("Rua Teste")
                .numero("123")
                .bairro("Centro")
                .cep("12345-678")
                .cidade("São Paulo")
                .estado("SP")
                .pais("Brasil")
                .build();
        pacienteRequest.setEndereco(enderecoRequest);

        Endereco endereco = new Endereco();
        endereco.setLogradouro("Rua Teste");
        paciente.setEndereco(endereco);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);

            when(pacienteRepository.find("cpf", pacienteRequest.getCpf())).thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg", pacienteRequest.getRg())).thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.persist(any(Paciente.class))).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> pacienteService.save(pacienteRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve falhar ao salvar paciente com CPF já existente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void savePacienteFalhaCpfExistente(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);

            when(pacienteRepository.find("cpf", pacienteRequest.getCpf())).thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().item(paciente));
            when(pacienteRepository.find("rg", pacienteRequest.getRg())).thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> pacienteService.save(pacienteRequest),
            throwable -> {
                assertInstanceOf(ConflictBusinessException.class, throwable);
                assertEquals("CPF ja existente!", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve falhar ao salvar paciente com RG já existente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void savePacienteFalhaRgExistente(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);

            when(pacienteRepository.find("cpf", pacienteRequest.getCpf())).thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg", pacienteRequest.getRg())).thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertFailedWith(
            () -> pacienteService.save(pacienteRequest),
            throwable -> {
                assertInstanceOf(ConflictBusinessException.class, throwable);
                assertEquals("RG ja existente!", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve salvar paciente com responsáveis (filiação)")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void savePacienteComResponsaveis(UniAsserter asserter) {
        FiliacaoRequest filiacaoRequest = FiliacaoRequest.builder()
                .nome("Maria da Silva")
                .cpf("98765432101")
                .telefone("11888888888")
                .email("maria@email.com")
                .grauDeParentesco("MÃE")
                .build();
        pacienteRequest.setResponsaveis(List.of(filiacaoRequest));

        Filiacao filiacao = new Filiacao();
        filiacao.setId(1L);
        filiacao.setNome("Maria da Silva");
        filiacao.setCpf("98765432101");

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);
            PanacheQuery<Filiacao> mockQueryFiliacao = mock(PanacheQuery.class);

            when(pacienteRepository.find("cpf", pacienteRequest.getCpf())).thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg", pacienteRequest.getRg())).thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.persist(any(Paciente.class))).thenReturn(Uni.createFrom().item(paciente));
            when(filiacaoRepository.find("cpf", filiacaoRequest.getCpf())).thenReturn(mockQueryFiliacao);
            when(mockQueryFiliacao.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(filiacaoRepository.persist(any(Filiacao.class))).thenReturn(Uni.createFrom().item(filiacao));
        });

        asserter.assertThat(
            () -> pacienteService.save(pacienteRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve buscar paciente por ID com sucesso")
    @RunOnVertxContext
    void findByIdComSucesso(UniAsserter asserter) {
        asserter.execute(() -> {
            when(pacienteRepository.findByIdWithCollections(1L)).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> pacienteService.findById(1L),
            result -> {
                assertNotNull(result);
                assertEquals(paciente.getId(), result.getId());
                assertEquals(paciente.getNome(), result.getNome());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException quando paciente não existe")
    @RunOnVertxContext
    void findByIdPacienteNaoEncontrado(UniAsserter asserter) {
        asserter.execute(() -> {
            when(pacienteRepository.findByIdWithCollections(1L)).thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> pacienteService.findById(1L),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Paciente não encontrado!", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve realizar soft delete do paciente com sucesso")
    @RunOnVertxContext
    void softDeleteComSucesso(UniAsserter asserter) {
        asserter.execute(() -> {
            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
            when(filiacaoRepository.update(anyString(), any(Object[].class))).thenReturn(Uni.createFrom().item(1));
            when(carteiraRepository.update(anyString(), any(Object[].class))).thenReturn(Uni.createFrom().item(1));
            when(prontuarioRepository.update(anyString(), any(Object[].class))).thenReturn(Uni.createFrom().item(1));
            when(anamneseRepository.update(anyString(), any(Object[].class))).thenReturn(Uni.createFrom().item(1));
        });

        asserter.assertThat(
            () -> pacienteService.softDelete(1L),
            result -> assertTrue(result)
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException ao soft delete de paciente inexistente")
    @RunOnVertxContext
    void softDeletePacienteNaoEncontrado(UniAsserter asserter) {
        asserter.execute(() -> {
            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> pacienteService.softDelete(1L),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Paciente não encontrado", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar UnprocessableEntityBusinessException ao soft delete de paciente já arquivado")
    @RunOnVertxContext
    void softDeletePacienteJaArquivado(UniAsserter asserter) {
        asserter.execute(() -> {
            paciente.setStatus(false);
            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertFailedWith(
            () -> pacienteService.softDelete(1L),
            throwable -> {
                assertInstanceOf(UnprocessableEntityBusinessException.class, throwable);
                assertEquals("Paciente já arquivado", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve restaurar paciente com sucesso")
    @RunOnVertxContext
    void restoreComSucesso(UniAsserter asserter) {
        asserter.execute(() -> {
            paciente.setStatus(false);
            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> pacienteService.restore(1L),
            result -> assertTrue(result)
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException ao restaurar paciente inexistente")
    @RunOnVertxContext
    void restorePacienteNaoEncontrado(UniAsserter asserter) {
        asserter.execute(() -> {
            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> pacienteService.restore(1L),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Paciente não encontrado", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar ConflictBusinessException ao restaurar paciente já ativo")
    @RunOnVertxContext
    void restorePacienteJaAtivo(UniAsserter asserter) {
        asserter.execute(() -> {
            paciente.setStatus(true);
            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertFailedWith(
            () -> pacienteService.restore(1L),
            throwable -> {
                assertInstanceOf(ConflictBusinessException.class, throwable);
                assertEquals("Paciente já ativo", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve atualizar paciente com sucesso")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateComSucesso(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);

            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
            when(pacienteRepository.find("cpf = ?1 and id <> ?2", pacienteRequest.getCpf(), 1L))
                    .thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg = ?1 and id <> ?2", pacienteRequest.getRg(), 1L))
                    .thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.findByIdWithCollections(1L)).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> pacienteService.update(1L, pacienteRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve atualizar paciente com endereco existente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateComEnderecoExistente(UniAsserter asserter) {
        Endereco endereco = new Endereco();
        endereco.setLogradouro("Rua Antiga");
        paciente.setEndereco(endereco);

        EnderecoRequest enderecoRequest = EnderecoRequest.builder()
                .logradouro("Rua Nova")
                .numero("456")
                .bairro("Novo Bairro")
                .cep("87654-321")
                .cidade("Rio de Janeiro")
                .estado("RJ")
                .pais("Brasil")
                .build();
        pacienteRequest.setEndereco(enderecoRequest);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);

            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
            when(pacienteRepository.find("cpf = ?1 and id <> ?2", pacienteRequest.getCpf(), 1L))
                    .thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg = ?1 and id <> ?2", pacienteRequest.getRg(), 1L))
                    .thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.findByIdWithCollections(1L)).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> pacienteService.update(1L, pacienteRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve atualizar paciente com novo endereco")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateComNovoEndereco(UniAsserter asserter) {
        paciente.setEndereco(null);

        EnderecoRequest enderecoRequest = EnderecoRequest.builder()
                .logradouro("Rua Nova")
                .numero("456")
                .bairro("Novo Bairro")
                .cep("87654-321")
                .cidade("Rio de Janeiro")
                .estado("RJ")
                .pais("Brasil")
                .build();
        pacienteRequest.setEndereco(enderecoRequest);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);

            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
            when(pacienteRepository.find("cpf = ?1 and id <> ?2", pacienteRequest.getCpf(), 1L))
                    .thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg = ?1 and id <> ?2", pacienteRequest.getRg(), 1L))
                    .thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.findByIdWithCollections(1L)).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> pacienteService.update(1L, pacienteRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException ao atualizar paciente inexistente")
    @RunOnVertxContext
    void updatePacienteNaoEncontrado(UniAsserter asserter) {
        asserter.execute(() -> {
            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> pacienteService.update(1L, pacienteRequest),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Paciente não encontrado", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar UnprocessableEntityBusinessException ao atualizar paciente arquivado")
    @RunOnVertxContext
    void updatePacienteArquivado(UniAsserter asserter) {
        asserter.execute(() -> {
            paciente.setStatus(false);
            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertFailedWith(
            () -> pacienteService.update(1L, pacienteRequest),
            throwable -> {
                assertInstanceOf(UnprocessableEntityBusinessException.class, throwable);
                assertEquals("Paciente arquivado, não é possível atualizar!", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar ConflictBusinessException ao atualizar com CPF já existente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateCpfJaExistente(UniAsserter asserter) {
        Paciente outroPaciente = new Paciente();
        outroPaciente.setId(2L);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);

            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
            when(pacienteRepository.find("cpf = ?1 and id <> ?2", pacienteRequest.getCpf(), 1L))
                    .thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().item(outroPaciente));
        });

        asserter.assertFailedWith(
            () -> pacienteService.update(1L, pacienteRequest),
            throwable -> {
                assertInstanceOf(ConflictBusinessException.class, throwable);
                assertEquals("CPF já existente!", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar ConflictBusinessException ao atualizar com RG já existente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateRgJaExistente(UniAsserter asserter) {
        Paciente outroPaciente = new Paciente();
        outroPaciente.setId(2L);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);

            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
            when(pacienteRepository.find("cpf = ?1 and id <> ?2", pacienteRequest.getCpf(), 1L))
                    .thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg = ?1 and id <> ?2", pacienteRequest.getRg(), 1L))
                    .thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().item(outroPaciente));
        });

        asserter.assertFailedWith(
            () -> pacienteService.update(1L, pacienteRequest),
            throwable -> {
                assertInstanceOf(ConflictBusinessException.class, throwable);
                assertEquals("RG já existente!", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar pacientes paginados com sucesso sem sort")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedSemSort(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        List<Paciente> pacientes = List.of(paciente);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.findPaginated(isNull(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(pacientes));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> pacienteService.findPaginated(page, null, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
                assertEquals(1L, result.getTotalCount());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar pacientes paginados com sort ascendente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComSortAsc(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "nome,asc";
        List<Paciente> pacientes = List.of(paciente);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.findPaginated(any(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(pacientes));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> pacienteService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar pacientes paginados com sort descendente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComSortDesc(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "nome,desc";
        List<Paciente> pacientes = List.of(paciente);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.findPaginated(any(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(pacientes));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> pacienteService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar pacientes paginados com filtros")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComFiltros(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        List<String> filterFields = List.of("nome");
        List<String> filterValues = List.of("João");
        List<Paciente> pacientes = List.of(paciente);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.findPaginated(isNull(), eq(filterFields), eq(filterValues))).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(pacientes));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> pacienteService.findPaginated(page, null, filterFields, filterValues),
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
                        pacienteService.findPaginated(page, sort, null, null)
                );
                assertEquals("Campo de ordenação invalido: campoInvalido", exception.getMessage());
                return Uni.createFrom().voidItem();
            },
            result -> {}
        );
    }

    @Test
    @DisplayName("Deve buscar pacientes paginados com sort apenas campo (default asc)")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComSortApenasCampo(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "id";
        List<Paciente> pacientes = List.of(paciente);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.findPaginated(any(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(pacientes));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> pacienteService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar pacientes paginados ignorando sort vazio")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComSortVazio(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "";
        List<Paciente> pacientes = List.of(paciente);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.findPaginated(isNull(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(pacientes));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> pacienteService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar pacientes paginados ignorando sort com espaços")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComSortEspacos(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "   ";
        List<Paciente> pacientes = List.of(paciente);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.findPaginated(isNull(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(pacientes));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> pacienteService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve atualizar filiacao existente vinculada ao paciente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateFiliacaoExistenteVinculada(UniAsserter asserter) {
        Filiacao filiacaoExistente = new Filiacao();
        filiacaoExistente.setId(1L);
        filiacaoExistente.setCpf("98765432101");
        filiacaoExistente.setNome("Maria da Silva");
        paciente.getResponsaveis().add(filiacaoExistente);

        FiliacaoRequest filiacaoRequest = FiliacaoRequest.builder()
                .nome("Maria da Silva Atualizada")
                .cpf("98765432101")
                .telefone("11888888888")
                .email("maria@email.com")
                .grauDeParentesco("MÃE")
                .build();
        pacienteRequest.setResponsaveis(List.of(filiacaoRequest));

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);
            PanacheQuery<Filiacao> mockQueryFiliacao = mock(PanacheQuery.class);

            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
            when(pacienteRepository.find("cpf = ?1 and id <> ?2", pacienteRequest.getCpf(), 1L))
                    .thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg = ?1 and id <> ?2", pacienteRequest.getRg(), 1L))
                    .thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.findByIdWithCollections(1L)).thenReturn(Uni.createFrom().item(paciente));
            when(filiacaoRepository.find("cpf", filiacaoRequest.getCpf())).thenReturn(mockQueryFiliacao);
            when(mockQueryFiliacao.firstResult()).thenReturn(Uni.createFrom().item(filiacaoExistente));
            when(filiacaoRepository.persist(any(Filiacao.class))).thenReturn(Uni.createFrom().item(filiacaoExistente));
        });

        asserter.assertThat(
            () -> pacienteService.update(1L, pacienteRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve lançar ConflictBusinessException ao atualizar filiacao com CPF já vinculado a outro paciente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateFiliacaoCpfJaVinculadoOutroPaciente(UniAsserter asserter) {
        Filiacao filiacaoExistente = new Filiacao();
        filiacaoExistente.setId(2L);
        filiacaoExistente.setCpf("98765432101");
        filiacaoExistente.setNome("Outra Pessoa");

        FiliacaoRequest filiacaoRequest = FiliacaoRequest.builder()
                .nome("Maria da Silva")
                .cpf("98765432101")
                .telefone("11888888888")
                .email("maria@email.com")
                .grauDeParentesco("MÃE")
                .build();
        pacienteRequest.setResponsaveis(List.of(filiacaoRequest));

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);
            PanacheQuery<Filiacao> mockQueryFiliacao = mock(PanacheQuery.class);

            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
            when(pacienteRepository.find("cpf = ?1 and id <> ?2", pacienteRequest.getCpf(), 1L))
                    .thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg = ?1 and id <> ?2", pacienteRequest.getRg(), 1L))
                    .thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.findByIdWithCollections(1L)).thenReturn(Uni.createFrom().item(paciente));
            when(filiacaoRepository.find("cpf", filiacaoRequest.getCpf())).thenReturn(mockQueryFiliacao);
            when(mockQueryFiliacao.firstResult()).thenReturn(Uni.createFrom().item(filiacaoExistente));
        });

        asserter.assertFailedWith(
            () -> pacienteService.update(1L, pacienteRequest),
            throwable -> {
                assertInstanceOf(ConflictBusinessException.class, throwable);
                assertEquals("CPF já existente!", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve atualizar paciente sem responsáveis (lista null)")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updatePacienteSemResponsaveisNull(UniAsserter asserter) {
        pacienteRequest.setResponsaveis(null);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);

            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
            when(pacienteRepository.find("cpf = ?1 and id <> ?2", pacienteRequest.getCpf(), 1L))
                    .thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg = ?1 and id <> ?2", pacienteRequest.getRg(), 1L))
                    .thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.findByIdWithCollections(1L)).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> pacienteService.update(1L, pacienteRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve atualizar paciente sem responsáveis (lista vazia)")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updatePacienteSemResponsaveisVazia(UniAsserter asserter) {
        pacienteRequest.setResponsaveis(List.of());

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);

            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
            when(pacienteRepository.find("cpf = ?1 and id <> ?2", pacienteRequest.getCpf(), 1L))
                    .thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg = ?1 and id <> ?2", pacienteRequest.getRg(), 1L))
                    .thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.findByIdWithCollections(1L)).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> pacienteService.update(1L, pacienteRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve atualizar paciente com responsáveis quando paciente.responsaveis é null")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updatePacienteComResponsaveisQuandoPacienteResponsaveisNull(UniAsserter asserter) {
        paciente.setResponsaveis(null);

        FiliacaoRequest filiacaoRequest = FiliacaoRequest.builder()
                .nome("Maria da Silva")
                .cpf("98765432101")
                .telefone("11888888888")
                .email("maria@email.com")
                .grauDeParentesco("MÃE")
                .build();
        pacienteRequest.setResponsaveis(List.of(filiacaoRequest));

        Filiacao novaFiliacao = new Filiacao();
        novaFiliacao.setId(1L);
        novaFiliacao.setCpf("98765432101");

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);
            PanacheQuery<Filiacao> mockQueryFiliacao = mock(PanacheQuery.class);

            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
            when(pacienteRepository.find("cpf = ?1 and id <> ?2", pacienteRequest.getCpf(), 1L))
                    .thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg = ?1 and id <> ?2", pacienteRequest.getRg(), 1L))
                    .thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.findByIdWithCollections(1L)).thenReturn(Uni.createFrom().item(paciente));
            when(filiacaoRepository.find("cpf", filiacaoRequest.getCpf())).thenReturn(mockQueryFiliacao);
            when(mockQueryFiliacao.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(filiacaoRepository.persist(any(Filiacao.class))).thenReturn(Uni.createFrom().item(novaFiliacao));
        });

        asserter.assertThat(
            () -> pacienteService.update(1L, pacienteRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve buscar pacientes paginados com todos os campos de ordenação válidos")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComTodosCamposOrdenacaoValidos(UniAsserter asserter) {
        List<String> camposValidos = List.of("id", "nome", "cpf", "rg", "dataNascimento", "sexo", "telefone", "email", "idade");
        Page page = Page.of(0, 10);
        List<Paciente> pacientes = List.of(paciente);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.findPaginated(any(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(pacientes));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        for (String campo : camposValidos) {
            asserter.assertThat(
                () -> pacienteService.findPaginated(page, campo + ",asc", null, null),
                result -> assertNotNull(result)
            );
        }
    }

    @Test
    @DisplayName("Deve salvar paciente com filiação existente vinculada ao mesmo paciente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void savePacienteComFiliacaoExistenteVinculada(UniAsserter asserter) {
        Filiacao filiacaoExistente = new Filiacao();
        filiacaoExistente.setId(1L);
        filiacaoExistente.setCpf("98765432101");
        filiacaoExistente.setNome("Maria da Silva");
        paciente.setResponsaveis(new HashSet<>(Set.of(filiacaoExistente)));

        FiliacaoRequest filiacaoRequest = FiliacaoRequest.builder()
                .nome("Maria da Silva Atualizada")
                .cpf("98765432101")
                .telefone("11888888888")
                .email("maria@email.com")
                .grauDeParentesco("MÃE")
                .idade(45)
                .build();
        pacienteRequest.setResponsaveis(List.of(filiacaoRequest));

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);
            PanacheQuery<Filiacao> mockQueryFiliacao = mock(PanacheQuery.class);

            when(pacienteRepository.find("cpf", pacienteRequest.getCpf())).thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg", pacienteRequest.getRg())).thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.persist(any(Paciente.class))).thenReturn(Uni.createFrom().item(paciente));
            when(filiacaoRepository.find("cpf", filiacaoRequest.getCpf())).thenReturn(mockQueryFiliacao);
            when(mockQueryFiliacao.firstResult()).thenReturn(Uni.createFrom().item(filiacaoExistente));
            when(filiacaoRepository.persist(any(Filiacao.class))).thenReturn(Uni.createFrom().item(filiacaoExistente));
        });

        asserter.assertThat(
            () -> pacienteService.save(pacienteRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve lançar ConflictBusinessException ao salvar paciente com filiação CPF já existente em outro paciente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void savePacienteComFiliacaoCpfJaExistenteOutroPaciente(UniAsserter asserter) {
        Filiacao filiacaoExistente = new Filiacao();
        filiacaoExistente.setId(99L);
        filiacaoExistente.setCpf("98765432101");
        filiacaoExistente.setNome("Outra Pessoa");

        FiliacaoRequest filiacaoRequest = FiliacaoRequest.builder()
                .nome("Maria da Silva")
                .cpf("98765432101")
                .telefone("11888888888")
                .email("maria@email.com")
                .grauDeParentesco("MÃE")
                .build();
        pacienteRequest.setResponsaveis(List.of(filiacaoRequest));

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);
            PanacheQuery<Filiacao> mockQueryFiliacao = mock(PanacheQuery.class);

            when(pacienteRepository.find("cpf", pacienteRequest.getCpf())).thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg", pacienteRequest.getRg())).thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.persist(any(Paciente.class))).thenReturn(Uni.createFrom().item(paciente));
            when(filiacaoRepository.find("cpf", filiacaoRequest.getCpf())).thenReturn(mockQueryFiliacao);
            when(mockQueryFiliacao.firstResult()).thenReturn(Uni.createFrom().item(filiacaoExistente));
        });

        asserter.assertFailedWith(
            () -> pacienteService.save(pacienteRequest),
            throwable -> {
                assertInstanceOf(ConflictBusinessException.class, throwable);
                assertEquals("CPF já existente!", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar pacientes paginados com múltiplos filtros")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComMultiplosFiltros(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        List<String> filterFields = List.of("nome", "cpf", "email");
        List<String> filterValues = List.of("João", "12345678901", "joao@email.com");
        List<Paciente> pacientes = List.of(paciente);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.findPaginated(isNull(), eq(filterFields), eq(filterValues))).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(pacientes));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> pacienteService.findPaginated(page, null, filterFields, filterValues),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar pacientes paginados com sort e filtros combinados")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComSortEFiltros(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "nome,asc";
        List<String> filterFields = List.of("nome");
        List<String> filterValues = List.of("João");
        List<Paciente> pacientes = List.of(paciente);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.findPaginated(any(), eq(filterFields), eq(filterValues))).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(pacientes));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> pacienteService.findPaginated(page, sort, filterFields, filterValues),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não existem pacientes")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedListaVazia(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        List<Paciente> pacientesVazio = List.of();

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.findPaginated(isNull(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(pacientesVazio));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(0L));
        });

        asserter.assertThat(
            () -> pacienteService.findPaginated(page, null, null, null),
            result -> {
                assertNotNull(result);
                assertTrue(result.getContent().isEmpty());
                assertEquals(0L, result.getTotalCount());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar pacientes na segunda página")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedSegundaPagina(UniAsserter asserter) {
        Page page = Page.of(1, 10);
        List<Paciente> pacientes = List.of(paciente);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.findPaginated(isNull(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(pacientes));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(15L));
        });

        asserter.assertThat(
            () -> pacienteService.findPaginated(page, null, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
                assertEquals(15L, result.getTotalCount());
            }
        );
    }

    @Test
    @DisplayName("Deve salvar paciente com múltiplos responsáveis")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void savePacienteComMultiplosResponsaveis(UniAsserter asserter) {
        FiliacaoRequest filiacaoRequest1 = FiliacaoRequest.builder()
                .nome("Maria da Silva")
                .cpf("98765432101")
                .telefone("11888888888")
                .email("maria@email.com")
                .grauDeParentesco("MÃE")
                .build();
        FiliacaoRequest filiacaoRequest2 = FiliacaoRequest.builder()
                .nome("José da Silva")
                .cpf("11122233344")
                .telefone("11777777777")
                .email("jose@email.com")
                .grauDeParentesco("PAI")
                .build();
        pacienteRequest.setResponsaveis(List.of(filiacaoRequest1, filiacaoRequest2));

        Filiacao filiacao1 = new Filiacao();
        filiacao1.setId(1L);
        filiacao1.setNome("Maria da Silva");
        filiacao1.setCpf("98765432101");

        Filiacao filiacao2 = new Filiacao();
        filiacao2.setId(2L);
        filiacao2.setNome("José da Silva");
        filiacao2.setCpf("11122233344");

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);
            PanacheQuery<Filiacao> mockQueryFiliacao1 = mock(PanacheQuery.class);
            PanacheQuery<Filiacao> mockQueryFiliacao2 = mock(PanacheQuery.class);

            when(pacienteRepository.find("cpf", pacienteRequest.getCpf())).thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg", pacienteRequest.getRg())).thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.persist(any(Paciente.class))).thenReturn(Uni.createFrom().item(paciente));
            when(filiacaoRepository.find("cpf", filiacaoRequest1.getCpf())).thenReturn(mockQueryFiliacao1);
            when(mockQueryFiliacao1.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(filiacaoRepository.find("cpf", filiacaoRequest2.getCpf())).thenReturn(mockQueryFiliacao2);
            when(mockQueryFiliacao2.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(filiacaoRepository.persist(any(Filiacao.class)))
                    .thenReturn(Uni.createFrom().item(filiacao1))
                    .thenReturn(Uni.createFrom().item(filiacao2));
        });

        asserter.assertThat(
            () -> pacienteService.save(pacienteRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve atualizar paciente com novo endereço quando endereço anterior é null")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updatePacienteComNovoEnderecoQuandoEnderecoNull(UniAsserter asserter) {
        paciente.setEndereco(null);

        EnderecoRequest enderecoRequest = EnderecoRequest.builder()
                .logradouro("Rua Nova")
                .numero("456")
                .bairro("Novo Bairro")
                .cep("87654-321")
                .complemento("Apto 101")
                .cidade("Rio de Janeiro")
                .estado("RJ")
                .pais("Brasil")
                .build();
        pacienteRequest.setEndereco(enderecoRequest);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);

            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
            when(pacienteRepository.find("cpf = ?1 and id <> ?2", pacienteRequest.getCpf(), 1L))
                    .thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg = ?1 and id <> ?2", pacienteRequest.getRg(), 1L))
                    .thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.findByIdWithCollections(1L)).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> pacienteService.update(1L, pacienteRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve atualizar paciente com múltiplos responsáveis novos")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updatePacienteComMultiplosResponsaveisNovos(UniAsserter asserter) {
        FiliacaoRequest filiacaoRequest1 = FiliacaoRequest.builder()
                .nome("Maria da Silva")
                .cpf("98765432101")
                .telefone("11888888888")
                .email("maria@email.com")
                .grauDeParentesco("MÃE")
                .build();
        FiliacaoRequest filiacaoRequest2 = FiliacaoRequest.builder()
                .nome("José da Silva")
                .cpf("11122233344")
                .telefone("11777777777")
                .email("jose@email.com")
                .grauDeParentesco("PAI")
                .build();
        pacienteRequest.setResponsaveis(List.of(filiacaoRequest1, filiacaoRequest2));

        Filiacao filiacao1 = new Filiacao();
        filiacao1.setId(1L);
        filiacao1.setCpf("98765432101");

        Filiacao filiacao2 = new Filiacao();
        filiacao2.setId(2L);
        filiacao2.setCpf("11122233344");

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);
            PanacheQuery<Filiacao> mockQueryFiliacao1 = mock(PanacheQuery.class);
            PanacheQuery<Filiacao> mockQueryFiliacao2 = mock(PanacheQuery.class);

            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
            when(pacienteRepository.find("cpf = ?1 and id <> ?2", pacienteRequest.getCpf(), 1L))
                    .thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg = ?1 and id <> ?2", pacienteRequest.getRg(), 1L))
                    .thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.findByIdWithCollections(1L)).thenReturn(Uni.createFrom().item(paciente));
            when(filiacaoRepository.find("cpf", filiacaoRequest1.getCpf())).thenReturn(mockQueryFiliacao1);
            when(mockQueryFiliacao1.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(filiacaoRepository.find("cpf", filiacaoRequest2.getCpf())).thenReturn(mockQueryFiliacao2);
            when(mockQueryFiliacao2.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(filiacaoRepository.persist(any(Filiacao.class)))
                    .thenReturn(Uni.createFrom().item(filiacao1))
                    .thenReturn(Uni.createFrom().item(filiacao2));
        });

        asserter.assertThat(
            () -> pacienteService.update(1L, pacienteRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve salvar paciente com endereço contendo complemento")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void savePacienteComEnderecoComplemento(UniAsserter asserter) {
        EnderecoRequest enderecoRequest = EnderecoRequest.builder()
                .logradouro("Rua Teste")
                .numero("123")
                .bairro("Centro")
                .cep("12345-678")
                .complemento("Apartamento 101, Bloco A")
                .cidade("São Paulo")
                .estado("SP")
                .pais("Brasil")
                .build();
        pacienteRequest.setEndereco(enderecoRequest);

        Endereco endereco = new Endereco();
        endereco.setLogradouro("Rua Teste");
        endereco.setComplemento("Apartamento 101, Bloco A");
        paciente.setEndereco(endereco);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);

            when(pacienteRepository.find("cpf", pacienteRequest.getCpf())).thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg", pacienteRequest.getRg())).thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.persist(any(Paciente.class))).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> pacienteService.save(pacienteRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve salvar paciente sem endereço")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void savePacienteSemEndereco(UniAsserter asserter) {
        pacienteRequest.setEndereco(null);
        paciente.setEndereco(null);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);

            when(pacienteRepository.find("cpf", pacienteRequest.getCpf())).thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg", pacienteRequest.getRg())).thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.persist(any(Paciente.class))).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> pacienteService.save(pacienteRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve atualizar paciente sem alterar endereço quando enderecoRequest é null")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updatePacienteSemAlterarEndereco(UniAsserter asserter) {
        Endereco enderecoExistente = new Endereco();
        enderecoExistente.setLogradouro("Rua Existente");
        paciente.setEndereco(enderecoExistente);
        pacienteRequest.setEndereco(null);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);

            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
            when(pacienteRepository.find("cpf = ?1 and id <> ?2", pacienteRequest.getCpf(), 1L))
                    .thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg = ?1 and id <> ?2", pacienteRequest.getRg(), 1L))
                    .thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.findByIdWithCollections(1L)).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> pacienteService.update(1L, pacienteRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve buscar pacientes paginados com página de tamanho diferente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComTamanhoPaginaDiferente(UniAsserter asserter) {
        Page page = Page.of(0, 5);
        List<Paciente> pacientes = List.of(paciente);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.findPaginated(isNull(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(pacientes));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> pacienteService.findPaginated(page, null, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar pacientes paginados com múltiplos resultados")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComMultiplosResultados(UniAsserter asserter) {
        Page page = Page.of(0, 10);

        Paciente paciente2 = new Paciente();
        paciente2.setId(2L);
        paciente2.setNome("Maria Santos");
        paciente2.setCpf("98765432100");
        paciente2.setRg("987654321");
        paciente2.setSexo(Sexo.FEMININO);
        paciente2.setStatus(true);
        paciente2.setResponsaveis(new HashSet<>());
        paciente2.setTransacao(new HashSet<>());
        paciente2.setProntuarioDoPaciente(new HashSet<>());

        List<Paciente> pacientes = List.of(paciente, paciente2);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQuery = mock(PanacheQuery.class);
            when(pacienteRepository.findPaginated(isNull(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(pacientes));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(2L));
        });

        asserter.assertThat(
            () -> pacienteService.findPaginated(page, null, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(2, result.getContent().size());
                assertEquals(2L, result.getTotalCount());
            }
        );
    }

    @Test
    @DisplayName("Deve salvar paciente com filiação contendo todos os campos")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void savePacienteComFiliacaoCompleta(UniAsserter asserter) {
        FiliacaoRequest filiacaoRequest = FiliacaoRequest.builder()
                .nome("Maria da Silva")
                .cpf("98765432101")
                .telefone("11888888888")
                .email("maria@email.com")
                .grauDeParentesco("MÃE")
                .idade(45)
                .build();
        pacienteRequest.setResponsaveis(List.of(filiacaoRequest));

        Filiacao filiacao = new Filiacao();
        filiacao.setId(1L);
        filiacao.setNome("Maria da Silva");
        filiacao.setCpf("98765432101");
        filiacao.setTelefone("11888888888");
        filiacao.setEmail("maria@email.com");
        filiacao.setGrauDeParentesco("MÃE");
        filiacao.setIdade(45);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);
            PanacheQuery<Filiacao> mockQueryFiliacao = mock(PanacheQuery.class);

            when(pacienteRepository.find("cpf", pacienteRequest.getCpf())).thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg", pacienteRequest.getRg())).thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.persist(any(Paciente.class))).thenReturn(Uni.createFrom().item(paciente));
            when(filiacaoRepository.find("cpf", filiacaoRequest.getCpf())).thenReturn(mockQueryFiliacao);
            when(mockQueryFiliacao.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(filiacaoRepository.persist(any(Filiacao.class))).thenReturn(Uni.createFrom().item(filiacao));
        });

        asserter.assertThat(
            () -> pacienteService.save(pacienteRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve atualizar endereço completo do paciente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateEnderecoCompletoDoPaciente(UniAsserter asserter) {
        Endereco enderecoExistente = new Endereco();
        enderecoExistente.setLogradouro("Rua Antiga");
        paciente.setEndereco(enderecoExistente);

        EnderecoRequest enderecoRequest = EnderecoRequest.builder()
                .logradouro("Rua Nova")
                .numero("456")
                .bairro("Novo Bairro")
                .cep("87654-321")
                .complemento("Sala 202")
                .cidade("Rio de Janeiro")
                .estado("RJ")
                .pais("Brasil")
                .build();
        pacienteRequest.setEndereco(enderecoRequest);

        asserter.execute(() -> {
            PanacheQuery<Paciente> mockQueryCpf = mock(PanacheQuery.class);
            PanacheQuery<Paciente> mockQueryRg = mock(PanacheQuery.class);

            when(pacienteRepository.findById(1L)).thenReturn(Uni.createFrom().item(paciente));
            when(pacienteRepository.find("cpf = ?1 and id <> ?2", pacienteRequest.getCpf(), 1L))
                    .thenReturn(mockQueryCpf);
            when(mockQueryCpf.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.find("rg = ?1 and id <> ?2", pacienteRequest.getRg(), 1L))
                    .thenReturn(mockQueryRg);
            when(mockQueryRg.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(pacienteRepository.findByIdWithCollections(1L)).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> pacienteService.update(1L, pacienteRequest),
            result -> assertNotNull(result)
        );
    }
}
