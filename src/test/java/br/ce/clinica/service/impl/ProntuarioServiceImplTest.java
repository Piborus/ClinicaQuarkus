package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.ProntuarioRequest;
import br.ce.clinica.dto.response.PanachePage;
import br.ce.clinica.dto.response.ProntuarioResponse;
import br.ce.clinica.dto.response.ProntuarioResumeResponse;
import br.ce.clinica.entity.Paciente;
import br.ce.clinica.entity.Prontuario;
import br.ce.clinica.exception.BadRequestBusinessException;
import br.ce.clinica.exception.ConflictBusinessException;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.repository.PacienteRepository;
import br.ce.clinica.repository.ProntuarioRepository;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class ProntuarioServiceImplTest {

    @Inject
    ProntuarioServiceImpl prontuarioService;

    @InjectMock
    ProntuarioRepository prontuarioRepository;

    @InjectMock
    PacienteRepository pacienteRepository;

    private Paciente paciente;
    private Prontuario prontuario;
    private ProntuarioRequest prontuarioRequest;

    @BeforeEach
    void setUp() {
        Mockito.reset(prontuarioRepository, pacienteRepository);
        
        paciente = new Paciente();
        paciente.setId(1L);
        paciente.setNome("Test Patient");
        paciente.setStatus(true);

        prontuario = new Prontuario();
        prontuario.setId(1L);
        prontuario.setTexto("Test text");
        prontuario.setPaciente(paciente);

        prontuarioRequest = new ProntuarioRequest();
        prontuarioRequest.setPacienteId(1L);
        prontuarioRequest.setTexto("New prontuario text");
    }

    @Test
    void testSave_Success() {
        @SuppressWarnings("unchecked")
        PanacheQuery<Paciente> pacienteQuery = mock(PanacheQuery.class);
        when(pacienteQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
        
        when(pacienteRepository.find(eq("id"), eq(1L))).thenReturn(pacienteQuery);
        when(prontuarioRepository.persist(any(Prontuario.class)))
                .thenReturn(Uni.createFrom().item(prontuario));

        ProntuarioResponse result = prontuarioService.save(prontuarioRequest)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertNotNull(result);
        assertEquals(prontuario.getId(), result.getId());
        assertEquals(prontuario.getTexto(), result.getTexto());
        verify(pacienteRepository).find(eq("id"), eq(1L));
        verify(prontuarioRepository).persist(any(Prontuario.class));
    }

    @Test
    void testSave_PacienteNotFound() {
        @SuppressWarnings("unchecked")
        PanacheQuery<Paciente> pacienteQuery = mock(PanacheQuery.class);
        when(pacienteQuery.firstResult()).thenReturn(Uni.createFrom().nullItem());
        
        when(pacienteRepository.find(eq("id"), eq(1L))).thenReturn(pacienteQuery);

        UniAssertSubscriber<ProntuarioResponse> subscriber = prontuarioService.save(prontuarioRequest)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.awaitFailure();
        Throwable failure = subscriber.getFailure();
        assertInstanceOf(NotFoundBusinessException.class, failure);
        assertEquals("Paciente nao encontrado", failure.getMessage());
    }

    @Test
    void testSave_PacienteInativo() {
        paciente.setStatus(false);
        
        @SuppressWarnings("unchecked")
        PanacheQuery<Paciente> pacienteQuery = mock(PanacheQuery.class);
        when(pacienteQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
        
        when(pacienteRepository.find(eq("id"), eq(1L))).thenReturn(pacienteQuery);

        UniAssertSubscriber<ProntuarioResponse> subscriber = prontuarioService.save(prontuarioRequest)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.awaitFailure();
        Throwable failure = subscriber.getFailure();
        assertInstanceOf(ConflictBusinessException.class, failure);
        assertEquals("Paciente inativo. Não é possível criar prontuario para paciente inativo.", failure.getMessage());
    }

    @Test
    void testFindById_Success() {
        when(prontuarioRepository.findById(eq(1L)))
                .thenReturn(Uni.createFrom().item(prontuario));

        ProntuarioResumeResponse result = prontuarioService.findById(1L)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertNotNull(result);
        assertEquals(prontuario.getId(), result.getId());
        assertEquals(prontuario.getTexto(), result.getTexto());
        verify(prontuarioRepository).findById(eq(1L));
    }

    @Test
    void testFindById_NotFound() {
        when(prontuarioRepository.findById(eq(1L)))
                .thenReturn(Uni.createFrom().nullItem());

        UniAssertSubscriber<ProntuarioResumeResponse> subscriber = prontuarioService.findById(1L)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.awaitFailure();
        Throwable failure = subscriber.getFailure();
        assertInstanceOf(NotFoundBusinessException.class, failure);
        assertEquals("Prontuario nao encontrado", failure.getMessage());
    }

    @Test
    void testFindById_PacienteInativo() {
        paciente.setStatus(false);
        when(prontuarioRepository.findById(eq(1L)))
                .thenReturn(Uni.createFrom().item(prontuario));

        UniAssertSubscriber<ProntuarioResumeResponse> subscriber = prontuarioService.findById(1L)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.awaitFailure();
        Throwable failure = subscriber.getFailure();
        assertInstanceOf(ConflictBusinessException.class, failure);
        assertEquals("Paciente inativo. Não é possível acessar prontuario de paciente inativo.", failure.getMessage());
    }

    @Test
    void testDeleteById_Success() {
        @SuppressWarnings("unchecked")
        PanacheQuery<Prontuario> prontuarioQuery = mock(PanacheQuery.class);
        when(prontuarioQuery.firstResult()).thenReturn(Uni.createFrom().item(prontuario));
        
        when(prontuarioRepository.find(eq("id"), eq(1L))).thenReturn(prontuarioQuery);
        when(prontuarioRepository.deleteById(eq(1L)))
                .thenReturn(Uni.createFrom().item(true));

        Boolean result = prontuarioService.deleteById(1L)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertTrue(result);
        verify(prontuarioRepository).find(eq("id"), eq(1L));
        verify(prontuarioRepository).deleteById(eq(1L));
    }

    @Test
    void testDeleteById_NotFound() {
        @SuppressWarnings("unchecked")
        PanacheQuery<Prontuario> prontuarioQuery = mock(PanacheQuery.class);
        when(prontuarioQuery.firstResult()).thenReturn(Uni.createFrom().nullItem());
        
        when(prontuarioRepository.find(eq("id"), eq(1L))).thenReturn(prontuarioQuery);

        UniAssertSubscriber<Boolean> subscriber = prontuarioService.deleteById(1L)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.awaitFailure();
        Throwable failure = subscriber.getFailure();
        assertInstanceOf(NotFoundBusinessException.class, failure);
        assertEquals("Prontuario do paciente nao encontrado", failure.getMessage());
    }

    @Test
    void testUpdate_Success() {
        @SuppressWarnings("unchecked")
        PanacheQuery<Paciente> pacienteQuery = mock(PanacheQuery.class);
        when(pacienteQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
        
        when(pacienteRepository.find(eq("id"), eq(1L))).thenReturn(pacienteQuery);
        when(prontuarioRepository.findById(eq(1L)))
                .thenReturn(Uni.createFrom().item(prontuario));

        ProntuarioResumeResponse result = prontuarioService.update(1L, prontuarioRequest)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertNotNull(result);
        assertEquals(prontuario.getId(), result.getId());
        verify(pacienteRepository).find(eq("id"), eq(1L));
        verify(prontuarioRepository).findById(eq(1L));
    }

    @Test
    void testUpdate_PacienteNotFound() {
        @SuppressWarnings("unchecked")
        PanacheQuery<Paciente> pacienteQuery = mock(PanacheQuery.class);
        when(pacienteQuery.firstResult()).thenReturn(Uni.createFrom().nullItem());
        
        when(pacienteRepository.find(eq("id"), eq(1L))).thenReturn(pacienteQuery);

        UniAssertSubscriber<ProntuarioResumeResponse> subscriber = prontuarioService.update(1L, prontuarioRequest)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.awaitFailure();
        Throwable failure = subscriber.getFailure();
        assertInstanceOf(NotFoundBusinessException.class, failure);
        assertEquals("Paciente nao encontrado", failure.getMessage());
    }

    @Test
    void testUpdate_PacienteInativo() {
        paciente.setStatus(false);
        
        @SuppressWarnings("unchecked")
        PanacheQuery<Paciente> pacienteQuery = mock(PanacheQuery.class);
        when(pacienteQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
        
        when(pacienteRepository.find(eq("id"), eq(1L))).thenReturn(pacienteQuery);

        UniAssertSubscriber<ProntuarioResumeResponse> subscriber = prontuarioService.update(1L, prontuarioRequest)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.awaitFailure();
        Throwable failure = subscriber.getFailure();
        assertInstanceOf(ConflictBusinessException.class, failure);
        assertEquals("Paciente inativo. Não é possível atualizar prontuario de paciente inativo.", failure.getMessage());
    }

    @Test
    void testUpdate_ProntuarioNotFound() {
        @SuppressWarnings("unchecked")
        PanacheQuery<Paciente> pacienteQuery = mock(PanacheQuery.class);
        when(pacienteQuery.firstResult()).thenReturn(Uni.createFrom().item(paciente));
        
        when(pacienteRepository.find(eq("id"), eq(1L))).thenReturn(pacienteQuery);
        when(prontuarioRepository.findById(eq(1L)))
                .thenReturn(Uni.createFrom().nullItem());

        UniAssertSubscriber<ProntuarioResumeResponse> subscriber = prontuarioService.update(1L, prontuarioRequest)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.awaitFailure();
        Throwable failure = subscriber.getFailure();
        assertInstanceOf(NotFoundBusinessException.class, failure);
        assertEquals("Prontuario do paciente nao encontrado", failure.getMessage());
    }

    @Test
    void testFindByIdWithPaciente_Success() {
        when(prontuarioRepository.findByIdWithPaciente(eq(1L)))
                .thenReturn(Uni.createFrom().item(prontuario));

        ProntuarioResponse result = prontuarioService.findByIdWithPaciente(1L)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertNotNull(result);
        assertEquals(prontuario.getId(), result.getId());
        verify(prontuarioRepository).findByIdWithPaciente(eq(1L));
    }

    @Test
    void testFindByIdWithPaciente_NotFound() {
        when(prontuarioRepository.findByIdWithPaciente(eq(1L)))
                .thenReturn(Uni.createFrom().nullItem());

        UniAssertSubscriber<ProntuarioResponse> subscriber = prontuarioService.findByIdWithPaciente(1L)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.awaitFailure();
        Throwable failure = subscriber.getFailure();
        assertInstanceOf(NotFoundBusinessException.class, failure);
        assertEquals("Prontuario nao encontrado", failure.getMessage());
    }

    @Test
    void testFindPaginated_WithSortAscending() {
        Page page = Page.of(0, 10);
        String sort = "id,asc";
        List<String> filterFields = null;
        List<String> filterValues = null;

        @SuppressWarnings("unchecked")
        PanacheQuery<Prontuario> query = mock(PanacheQuery.class);
        @SuppressWarnings("unchecked")
        PanacheQuery<Prontuario> pagedQuery = mock(PanacheQuery.class);
        
        when(query.page(any(Page.class))).thenReturn(pagedQuery);
        when(pagedQuery.list()).thenReturn(Uni.createFrom().item(Arrays.asList(prontuario)));
        when(query.count()).thenReturn(Uni.createFrom().item(1L));

        when(prontuarioRepository.findPaginated(any(Sort.class), eq(filterFields), eq(filterValues)))
                .thenReturn(query);

        PanachePage<ProntuarioResponse> result = prontuarioService.findPaginated(page, sort, filterFields, filterValues)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalCount());
        verify(prontuarioRepository).findPaginated(any(Sort.class), eq(filterFields), eq(filterValues));
    }

    @Test
    void testFindPaginated_WithSortDescending() {
        Page page = Page.of(0, 10);
        String sort = "texto,desc";
        List<String> filterFields = null;
        List<String> filterValues = null;

        @SuppressWarnings("unchecked")
        PanacheQuery<Prontuario> query = mock(PanacheQuery.class);
        @SuppressWarnings("unchecked")
        PanacheQuery<Prontuario> pagedQuery = mock(PanacheQuery.class);
        
        when(query.page(any(Page.class))).thenReturn(pagedQuery);
        when(pagedQuery.list()).thenReturn(Uni.createFrom().item(Arrays.asList(prontuario)));
        when(query.count()).thenReturn(Uni.createFrom().item(1L));

        when(prontuarioRepository.findPaginated(any(Sort.class), eq(filterFields), eq(filterValues)))
                .thenReturn(query);

        PanachePage<ProntuarioResponse> result = prontuarioService.findPaginated(page, sort, filterFields, filterValues)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(prontuarioRepository).findPaginated(any(Sort.class), eq(filterFields), eq(filterValues));
    }

    @Test
    void testFindPaginated_WithoutSort() {
        Page page = Page.of(0, 10);
        String sort = null;
        List<String> filterFields = null;
        List<String> filterValues = null;

        @SuppressWarnings("unchecked")
        PanacheQuery<Prontuario> query = mock(PanacheQuery.class);
        @SuppressWarnings("unchecked")
        PanacheQuery<Prontuario> pagedQuery = mock(PanacheQuery.class);
        
        when(query.page(any(Page.class))).thenReturn(pagedQuery);
        when(pagedQuery.list()).thenReturn(Uni.createFrom().item(Arrays.asList(prontuario)));
        when(query.count()).thenReturn(Uni.createFrom().item(1L));

        when(prontuarioRepository.findPaginated(isNull(), eq(filterFields), eq(filterValues)))
                .thenReturn(query);

        PanachePage<ProntuarioResponse> result = prontuarioService.findPaginated(page, sort, filterFields, filterValues)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(prontuarioRepository).findPaginated(isNull(), eq(filterFields), eq(filterValues));
    }

    @Test
    void testFindPaginated_WithBlankSort() {
        Page page = Page.of(0, 10);
        String sort = "   ";
        List<String> filterFields = null;
        List<String> filterValues = null;

        @SuppressWarnings("unchecked")
        PanacheQuery<Prontuario> query = mock(PanacheQuery.class);
        @SuppressWarnings("unchecked")
        PanacheQuery<Prontuario> pagedQuery = mock(PanacheQuery.class);
        
        when(query.page(any(Page.class))).thenReturn(pagedQuery);
        when(pagedQuery.list()).thenReturn(Uni.createFrom().item(Arrays.asList(prontuario)));
        when(query.count()).thenReturn(Uni.createFrom().item(1L));

        when(prontuarioRepository.findPaginated(isNull(), eq(filterFields), eq(filterValues)))
                .thenReturn(query);

        PanachePage<ProntuarioResponse> result = prontuarioService.findPaginated(page, sort, filterFields, filterValues)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertNotNull(result);
        verify(prontuarioRepository).findPaginated(isNull(), eq(filterFields), eq(filterValues));
    }

    @Test
    void testFindPaginated_InvalidSortField() {
        Page page = Page.of(0, 10);
        String sort = "invalid_field,asc";
        List<String> filterFields = null;
        List<String> filterValues = null;

        assertThrows(BadRequestBusinessException.class, () -> {
            prontuarioService.findPaginated(page, sort, filterFields, filterValues)
                    .subscribe().withSubscriber(UniAssertSubscriber.create())
                    .awaitItem();
        });
    }

    @Test
    void testFindPaginated_WithFilters() {
        Page page = Page.of(0, 10);
        String sort = "id,asc";
        List<String> filterFields = Arrays.asList("texto");
        List<String> filterValues = Arrays.asList("test");

        @SuppressWarnings("unchecked")
        PanacheQuery<Prontuario> query = mock(PanacheQuery.class);
        @SuppressWarnings("unchecked")
        PanacheQuery<Prontuario> pagedQuery = mock(PanacheQuery.class);
        
        when(query.page(any(Page.class))).thenReturn(pagedQuery);
        when(pagedQuery.list()).thenReturn(Uni.createFrom().item(Arrays.asList(prontuario)));
        when(query.count()).thenReturn(Uni.createFrom().item(1L));

        when(prontuarioRepository.findPaginated(any(Sort.class), eq(filterFields), eq(filterValues)))
                .thenReturn(query);

        PanachePage<ProntuarioResponse> result = prontuarioService.findPaginated(page, sort, filterFields, filterValues)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(prontuarioRepository).findPaginated(any(Sort.class), eq(filterFields), eq(filterValues));
    }

    @Test
    void testFindPaginated_EmptyResults() {
        Page page = Page.of(0, 10);
        String sort = null;
        List<String> filterFields = null;
        List<String> filterValues = null;

        @SuppressWarnings("unchecked")
        PanacheQuery<Prontuario> query = mock(PanacheQuery.class);
        @SuppressWarnings("unchecked")
        PanacheQuery<Prontuario> pagedQuery = mock(PanacheQuery.class);
        
        when(query.page(any(Page.class))).thenReturn(pagedQuery);
        when(pagedQuery.list()).thenReturn(Uni.createFrom().item(Arrays.asList()));
        when(query.count()).thenReturn(Uni.createFrom().item(0L));

        when(prontuarioRepository.findPaginated(isNull(), eq(filterFields), eq(filterValues)))
                .thenReturn(query);

        PanachePage<ProntuarioResponse> result = prontuarioService.findPaginated(page, sort, filterFields, filterValues)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .getItem();

        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        assertEquals(0L, result.getTotalCount());
    }
}
