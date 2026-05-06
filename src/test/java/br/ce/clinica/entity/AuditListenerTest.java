package br.ce.clinica.entity;

import io.quarkus.security.identity.SecurityIdentity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("AuditListener Unit Tests")
class AuditListenerTest {

    @Test
    @DisplayName("Deve usar o nome do usuário autenticado no preenchimento de auditoria")
    void deveUsarNomeDoUsuarioAutenticado() throws Exception {
        AuditListener listener = new AuditListener();
        SecurityIdentity securityIdentity = mock(SecurityIdentity.class);
        Principal principal = () -> "joao@email.com";

        when(securityIdentity.isAnonymous()).thenReturn(false);
        when(securityIdentity.getPrincipal()).thenReturn(principal);
        injectSecurityIdentity(listener, securityIdentity);

        TestAuditEntity entity = new TestAuditEntity();
        listener.setCreationDate(entity);
        listener.setUpdateDate(entity);

        assertNotNull(entity.getDataCriacao());
        assertNotNull(entity.getDataAtualizacao());
        assertEquals("joao@email.com", entity.getCriadoPor());
        assertEquals("joao@email.com", entity.getAtualizadoPor());
        assertFalse(entity.getDeletado());
        assertEquals(Boolean.TRUE, entity.getStatus());
    }

    @Test
    @DisplayName("Deve usar system quando não houver identidade autenticada")
    void deveUsarSystemQuandoNaoHouverIdentidade() throws Exception {
        AuditListener listener = new AuditListener();
        SecurityIdentity securityIdentity = mock(SecurityIdentity.class);

        when(securityIdentity.isAnonymous()).thenReturn(true);
        when(securityIdentity.getPrincipal()).thenReturn(null);
        injectSecurityIdentity(listener, securityIdentity);

        TestAuditEntity entity = new TestAuditEntity();
        listener.setCreationDate(entity);

        assertEquals("system", entity.getCriadoPor());
        assertEquals("system", entity.getAtualizadoPor());
    }

    private static void injectSecurityIdentity(AuditListener listener, SecurityIdentity securityIdentity) throws Exception {
        Field field = AuditListener.class.getDeclaredField("securityIdentity");
        field.setAccessible(true);
        field.set(listener, securityIdentity);
    }

    private static class TestAuditEntity extends BaseAuditEntity {
    }
}


