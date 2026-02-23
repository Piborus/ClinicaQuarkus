package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.AnamneseDesenvolvimentoRequest;
import br.ce.clinica.dto.request.AnamneseRequest;
import br.ce.clinica.dto.request.AntecedenteFamiliarRequest;
import br.ce.clinica.entity.Anamnese;
import br.ce.clinica.entity.AnamneseDesenvolvimento;
import br.ce.clinica.entity.AntecedenteFamiliar;
import br.ce.clinica.entity.Paciente;
import br.ce.clinica.enums.TipoAnamnese;
import br.ce.clinica.exception.BadRequestBusinessException;
import br.ce.clinica.exception.ConflictBusinessException;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.exception.UnprocessableEntityBusinessException;
import br.ce.clinica.repository.AnamneseDesenvolvimentoRepository;
import br.ce.clinica.repository.AnamneseRepository;
import br.ce.clinica.repository.AntecedenteFamiliarRepository;
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

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("AnamneseServiceImpl Unit Tests")
class AnamneseServiceImplTest {

    @InjectMock
    AnamneseRepository anamneseRepository;

    @InjectMock
    PacienteRepository pacienteRepository;

    @InjectMock
    AnamneseDesenvolvimentoRepository desenvolvimentoRepository;

    @InjectMock
    AntecedenteFamiliarRepository familiarRepository;

    @Inject
    AnamneseServiceImpl anamneseService;

    private Paciente paciente;
    private Anamnese anamnese;
    private AnamneseRequest anamneseRequest;

    @BeforeEach
    void setUp() {
        paciente = new Paciente();
        paciente.setId(1L);
        paciente.setNome("João da Silva");
        paciente.setStatus(true);
        paciente.setResponsaveis(new HashSet<>());
        paciente.setTransacao(new HashSet<>());
        paciente.setProntuarioDoPaciente(new HashSet<>());

        anamnese = new Anamnese();
        anamnese.setId(1L);
        anamnese.setTipoAnamnese(TipoAnamnese.INICIAL);
        anamnese.setEncaminhamento("Hospital");
        anamnese.setHistoricoAcompanhamento("Paciente acompanhado por psicólogo");
        anamnese.setPsicodinamicaFamiliar("Paciente com problemas de autoconhecimento");
        anamnese.setObservacao("Observação teste");
        anamnese.setPaciente(paciente);

        anamneseRequest = AnamneseRequest.builder()
                .pacienteId(1L)
                .encaminhamento("Hospital")
                .historicoAcompanhamento("Paciente acompanhado por psicólogo")
                .psicodinamicaFamiliar("Paciente com problemas de autoconhecimento")
                .observacao("Observação teste")
                .build();
    }

    @Test
    @DisplayName("Deve salvar anamnese com sucesso quando paciente está ativo")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void saveAnamneseComSucesso(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQueryAnamnese = mock(PanacheQuery.class);
            when(pacienteRepository.findByIdWithCollections(anamneseRequest.getPacienteId()))
                    .thenReturn(Uni.createFrom().item(paciente));
            when(anamneseRepository.find("paciente.id", paciente.getId())).thenReturn(mockQueryAnamnese);
            when(mockQueryAnamnese.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(anamneseRepository.persist(any(Anamnese.class))).thenReturn(Uni.createFrom().item(anamnese));
        });

        asserter.assertThat(
            () -> anamneseService.save(anamneseRequest),
            result -> {
                assertNotNull(result);
                assertEquals(anamnese.getId(), result.getId());
                assertEquals(TipoAnamnese.INICIAL, result.getTipoAnamnese());
                assertEquals(anamnese.getEncaminhamento(), result.getEncaminhamento());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException quando paciente não existe ao salvar")
    @RunOnVertxContext
    void saveFalhaPacienteNaoEncontrado(UniAsserter asserter) {
        asserter.execute(() -> {
            when(pacienteRepository.findByIdWithCollections(anamneseRequest.getPacienteId()))
                    .thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> anamneseService.save(anamneseRequest),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Paciente não encontrado.", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar UnprocessableEntityBusinessException quando paciente está inativo ao salvar")
    @RunOnVertxContext
    void saveFalhaPacienteInativo(UniAsserter asserter) {
        asserter.execute(() -> {
            paciente.setStatus(false);
            when(pacienteRepository.findByIdWithCollections(anamneseRequest.getPacienteId()))
                    .thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertFailedWith(
            () -> anamneseService.save(anamneseRequest),
            throwable -> {
                assertInstanceOf(UnprocessableEntityBusinessException.class, throwable);
                assertEquals("Paciente inativo. Não é possível cadastrar anamnese para paciente inativo.", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar ConflictBusinessException quando paciente já possui anamnese")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void saveFalhaPacienteJaPossuiAnamnese(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQueryAnamnese = mock(PanacheQuery.class);
            when(pacienteRepository.findByIdWithCollections(anamneseRequest.getPacienteId()))
                    .thenReturn(Uni.createFrom().item(paciente));
            when(anamneseRepository.find("paciente.id", paciente.getId())).thenReturn(mockQueryAnamnese);
            when(mockQueryAnamnese.firstResult()).thenReturn(Uni.createFrom().item(anamnese));
        });

        asserter.assertFailedWith(
            () -> anamneseService.save(anamneseRequest),
            throwable -> {
                assertInstanceOf(ConflictBusinessException.class, throwable);
                assertEquals("Paciente já possui anamnese cadastrada.", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve salvar anamnese com desenvolvimento")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void saveAnamneseComDesenvolvimento(UniAsserter asserter) {
        AnamneseDesenvolvimentoRequest desenvolvimentoRequest = AnamneseDesenvolvimentoRequest.builder()
                .gravidezParto("Normal")
                .memoriasInfancia("Boa infância")
                .memoriasAdolescencia("Adolescência tranquila")
                .faseAdulta("Fase adulta estável")
                .faseAtual("Fase atual boa")
                .moraComQuem("Família")
                .numeroFilhos(2)
                .numeroIrmaos(1)
                .ordemNascimento("primeiro")
                .fumante(false)
                .etilista(false)
                .usoMedicamento(false)
                .descricaoMedicamentos(null)
                .rotina("Rotina normal")
                .build();
        anamneseRequest.setDesenvolvimento(desenvolvimentoRequest);

        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQueryAnamnese = mock(PanacheQuery.class);
            when(pacienteRepository.findByIdWithCollections(anamneseRequest.getPacienteId()))
                    .thenReturn(Uni.createFrom().item(paciente));
            when(anamneseRepository.find("paciente.id", paciente.getId())).thenReturn(mockQueryAnamnese);
            when(mockQueryAnamnese.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(anamneseRepository.persist(any(Anamnese.class))).thenReturn(Uni.createFrom().item(anamnese));
        });

        asserter.assertThat(
            () -> anamneseService.save(anamneseRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve salvar anamnese com antecedente familiar")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void saveAnamneseComAntecedenteFamiliar(UniAsserter asserter) {
        AntecedenteFamiliarRequest antecedenteFamiliarRequest = AntecedenteFamiliarRequest.builder()
                .tiposAntecedentes(List.of())
                .descricao("Descrição do antecedente familiar")
                .build();
        anamneseRequest.setAntecedenteFamiliar(antecedenteFamiliarRequest);

        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQueryAnamnese = mock(PanacheQuery.class);
            when(pacienteRepository.findByIdWithCollections(anamneseRequest.getPacienteId()))
                    .thenReturn(Uni.createFrom().item(paciente));
            when(anamneseRepository.find("paciente.id", paciente.getId())).thenReturn(mockQueryAnamnese);
            when(mockQueryAnamnese.firstResult()).thenReturn(Uni.createFrom().nullItem());
            when(anamneseRepository.persist(any(Anamnese.class))).thenReturn(Uni.createFrom().item(anamnese));
        });

        asserter.assertThat(
            () -> anamneseService.save(anamneseRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve atualizar anamnese com sucesso")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateAnamneseComSucesso(UniAsserter asserter) {
        AnamneseRequest updateRequest = AnamneseRequest.builder()
                .pacienteId(1L)
                .encaminhamento("Hospital Atualizado")
                .historicoAcompanhamento("Histórico atualizado")
                .psicodinamicaFamiliar("Psicodinâmica atualizada")
                .observacao("Observação atualizada")
                .build();

        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQueryAnamnese = mock(PanacheQuery.class);
            when(anamneseRepository.find("id", 1L)).thenReturn(mockQueryAnamnese);
            when(mockQueryAnamnese.firstResult()).thenReturn(Uni.createFrom().item(anamnese));
            when(pacienteRepository.findById(paciente.getId())).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> anamneseService.update(1L, updateRequest),
            result -> {
                assertNotNull(result);
                assertEquals(TipoAnamnese.REAVALIACAO, result.getTipoAnamnese());
                assertEquals(updateRequest.getEncaminhamento(), result.getEncaminhamento());
                assertEquals(updateRequest.getHistoricoAcompanhamento(), result.getHistoricoAcompanhamento());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException quando anamnese não existe ao atualizar")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateFalhaAnamneseNaoEncontrada(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQueryAnamnese = mock(PanacheQuery.class);
            when(anamneseRepository.find("id", 1L)).thenReturn(mockQueryAnamnese);
            when(mockQueryAnamnese.firstResult()).thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> anamneseService.update(1L, anamneseRequest),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Anamnese não encontrada.", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException quando paciente não existe ao atualizar")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateFalhaPacienteNaoEncontrado(UniAsserter asserter) {
        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQueryAnamnese = mock(PanacheQuery.class);
            when(anamneseRepository.find("id", 1L)).thenReturn(mockQueryAnamnese);
            when(mockQueryAnamnese.firstResult()).thenReturn(Uni.createFrom().item(anamnese));
            when(pacienteRepository.findById(paciente.getId())).thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> anamneseService.update(1L, anamneseRequest),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Paciente não encontrado.", throwable.getMessage());
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
            PanacheQuery<Anamnese> mockQueryAnamnese = mock(PanacheQuery.class);
            when(anamneseRepository.find("id", 1L)).thenReturn(mockQueryAnamnese);
            when(mockQueryAnamnese.firstResult()).thenReturn(Uni.createFrom().item(anamnese));
            when(pacienteRepository.findById(paciente.getId())).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertFailedWith(
            () -> anamneseService.update(1L, anamneseRequest),
            throwable -> {
                assertInstanceOf(UnprocessableEntityBusinessException.class, throwable);
                assertEquals("Paciente inativo. Não é possível atualizar anamnese para paciente inativo.", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve atualizar anamnese com desenvolvimento existente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateAnamneseComDesenvolvimentoExistente(UniAsserter asserter) {
        AnamneseDesenvolvimento desenvolvimento = new AnamneseDesenvolvimento();
        desenvolvimento.setId(1L);
        desenvolvimento.setGravidezParto("Normal");
        anamnese.setDesenvolvimento(desenvolvimento);

        AnamneseDesenvolvimentoRequest desenvolvimentoRequest = AnamneseDesenvolvimentoRequest.builder()
                .gravidezParto("Parto cesariano")
                .memoriasInfancia("Infância atualizada")
                .build();
        anamneseRequest.setDesenvolvimento(desenvolvimentoRequest);

        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQueryAnamnese = mock(PanacheQuery.class);
            when(anamneseRepository.find("id", 1L)).thenReturn(mockQueryAnamnese);
            when(mockQueryAnamnese.firstResult()).thenReturn(Uni.createFrom().item(anamnese));
            when(pacienteRepository.findById(paciente.getId())).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> anamneseService.update(1L, anamneseRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve atualizar anamnese com antecedente familiar existente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateAnamneseComAntecedenteFamiliarExistente(UniAsserter asserter) {
        AntecedenteFamiliar antecedenteFamiliar = new AntecedenteFamiliar();
        antecedenteFamiliar.setId(1L);
        antecedenteFamiliar.setDescricao("Descrição original");
        anamnese.setAntecedenteFamiliar(antecedenteFamiliar);

        AntecedenteFamiliarRequest antecedenteFamiliarRequest = AntecedenteFamiliarRequest.builder()
                .descricao("Descrição atualizada")
                .build();
        anamneseRequest.setAntecedenteFamiliar(antecedenteFamiliarRequest);

        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQueryAnamnese = mock(PanacheQuery.class);
            when(anamneseRepository.find("id", 1L)).thenReturn(mockQueryAnamnese);
            when(mockQueryAnamnese.firstResult()).thenReturn(Uni.createFrom().item(anamnese));
            when(pacienteRepository.findById(paciente.getId())).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> anamneseService.update(1L, anamneseRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve buscar anamnese por ID com sucesso")
    @RunOnVertxContext
    void findByIdComSucesso(UniAsserter asserter) {
        asserter.execute(() -> {
            when(anamneseRepository.findByIdWithCollections(1L)).thenReturn(Uni.createFrom().item(anamnese));
        });

        asserter.assertThat(
            () -> anamneseService.findById(1L),
            result -> {
                assertNotNull(result);
                assertEquals(anamnese.getId(), result.getId());
                assertEquals(anamnese.getTipoAnamnese(), result.getTipoAnamnese());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundBusinessException quando anamnese não existe ao buscar por ID")
    @RunOnVertxContext
    void findByIdAnamneseNaoEncontrada(UniAsserter asserter) {
        asserter.execute(() -> {
            when(anamneseRepository.findByIdWithCollections(1L)).thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> anamneseService.findById(1L),
            throwable -> {
                assertInstanceOf(NotFoundBusinessException.class, throwable);
                assertEquals("Anamnese não encontrada.", throwable.getMessage());
            }
        );
    }


    @Test
    @DisplayName("Deve buscar anamneses paginadas com sucesso sem sort")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedSemSort(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        List<Anamnese> anamneses = List.of(anamnese);

        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQuery = mock(PanacheQuery.class);
            when(anamneseRepository.findPaginated(isNull(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(anamneses));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> anamneseService.findPaginated(page, null, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
                assertEquals(1L, result.getTotalCount());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar anamneses paginadas com sort ascendente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComSortAsc(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "id,asc";
        List<Anamnese> anamneses = List.of(anamnese);

        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQuery = mock(PanacheQuery.class);
            when(anamneseRepository.findPaginated(any(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(anamneses));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> anamneseService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar anamneses paginadas com sort descendente")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComSortDesc(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "id,desc";
        List<Anamnese> anamneses = List.of(anamnese);

        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQuery = mock(PanacheQuery.class);
            when(anamneseRepository.findPaginated(any(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(anamneses));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> anamneseService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar anamneses paginadas com filtros")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComFiltros(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        List<String> filterFields = List.of("encaminhamento");
        List<String> filterValues = List.of("Hospital");
        List<Anamnese> anamneses = List.of(anamnese);

        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQuery = mock(PanacheQuery.class);
            when(anamneseRepository.findPaginated(isNull(), eq(filterFields), eq(filterValues))).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(anamneses));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> anamneseService.findPaginated(page, null, filterFields, filterValues),
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
                        anamneseService.findPaginated(page, sort, null, null)
                );
                assertEquals("Campo de ordenação invalido: campoInvalido", exception.getMessage());
                return Uni.createFrom().voidItem();
            },
            result -> {}
        );
    }

    @Test
    @DisplayName("Deve buscar anamneses paginadas com sort apenas campo (default asc)")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComSortApenasCampo(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "id";
        List<Anamnese> anamneses = List.of(anamnese);

        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQuery = mock(PanacheQuery.class);
            when(anamneseRepository.findPaginated(any(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(anamneses));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> anamneseService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar anamneses paginadas ignorando sort vazio")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComSortVazio(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "";
        List<Anamnese> anamneses = List.of(anamnese);

        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQuery = mock(PanacheQuery.class);
            when(anamneseRepository.findPaginated(isNull(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(anamneses));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> anamneseService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar anamneses paginadas ignorando sort com espaços")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComSortEspacos(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        String sort = "   ";
        List<Anamnese> anamneses = List.of(anamnese);

        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQuery = mock(PanacheQuery.class);
            when(anamneseRepository.findPaginated(isNull(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(anamneses));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        asserter.assertThat(
            () -> anamneseService.findPaginated(page, sort, null, null),
            result -> {
                assertNotNull(result);
                assertEquals(1, result.getContent().size());
            }
        );
    }

    @Test
    @DisplayName("Deve buscar anamneses paginadas com todos os campos de ordenação válidos")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedComTodosCamposOrdenacaoValidos(UniAsserter asserter) {
        List<String> camposValidos = List.of("id", "tipoAnamnese", "encaminhamento", "historicoAcompanhamento",
                "psicodinamicaFamiliar", "observacao", "dataCriacao", "dataAtualizacao");
        Page page = Page.of(0, 10);
        List<Anamnese> anamneses = List.of(anamnese);

        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQuery = mock(PanacheQuery.class);
            when(anamneseRepository.findPaginated(any(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(anamneses));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(1L));
        });

        for (String campo : camposValidos) {
            asserter.assertThat(
                () -> anamneseService.findPaginated(page, campo + ",asc", null, null),
                result -> assertNotNull(result)
            );
        }
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não existem anamneses")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void findPaginatedListaVazia(UniAsserter asserter) {
        Page page = Page.of(0, 10);
        List<Anamnese> anamnesesVazia = List.of();

        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQuery = mock(PanacheQuery.class);
            when(anamneseRepository.findPaginated(isNull(), isNull(), isNull())).thenReturn(mockQuery);
            when(mockQuery.page(page)).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Uni.createFrom().item(anamnesesVazia));
            when(mockQuery.count()).thenReturn(Uni.createFrom().item(0L));
        });

        asserter.assertThat(
            () -> anamneseService.findPaginated(page, null, null, null),
            result -> {
                assertNotNull(result);
                assertTrue(result.getContent().isEmpty());
                assertEquals(0L, result.getTotalCount());
            }
        );
    }

    @Test
    @DisplayName("Deve criar novo desenvolvimento quando anamnese não possui")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateAnamnesesCriandoNovoDesenvolvimento(UniAsserter asserter) {
        anamnese.setDesenvolvimento(null);

        AnamneseDesenvolvimentoRequest desenvolvimentoRequest = AnamneseDesenvolvimentoRequest.builder()
                .gravidezParto("Parto normal")
                .memoriasInfancia("Boas memórias")
                .build();
        anamneseRequest.setDesenvolvimento(desenvolvimentoRequest);

        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQueryAnamnese = mock(PanacheQuery.class);
            when(anamneseRepository.find("id", 1L)).thenReturn(mockQueryAnamnese);
            when(mockQueryAnamnese.firstResult()).thenReturn(Uni.createFrom().item(anamnese));
            when(pacienteRepository.findById(paciente.getId())).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> anamneseService.update(1L, anamneseRequest),
            result -> assertNotNull(result)
        );
    }

    @Test
    @DisplayName("Deve criar novo antecedente familiar quando anamnese não possui")
    @SuppressWarnings("unchecked")
    @RunOnVertxContext
    void updateAnamneseCriandoNovoAntecedenteFamiliar(UniAsserter asserter) {
        anamnese.setAntecedenteFamiliar(null);

        AntecedenteFamiliarRequest antecedenteFamiliarRequest = AntecedenteFamiliarRequest.builder()
                .descricao("Nova descrição")
                .tiposAntecedentes(null)
                .build();
        anamneseRequest.setAntecedenteFamiliar(antecedenteFamiliarRequest);

        asserter.execute(() -> {
            PanacheQuery<Anamnese> mockQueryAnamnese = mock(PanacheQuery.class);
            when(anamneseRepository.find("id", 1L)).thenReturn(mockQueryAnamnese);
            when(mockQueryAnamnese.firstResult()).thenReturn(Uni.createFrom().item(anamnese));
            when(pacienteRepository.findById(paciente.getId())).thenReturn(Uni.createFrom().item(paciente));
        });

        asserter.assertThat(
            () -> anamneseService.update(1L, anamneseRequest),
            result -> assertNotNull(result)
        );
    }
}

