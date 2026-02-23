package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.LoginRequest;
import br.ce.clinica.dto.request.UsuarioRequest;
import br.ce.clinica.entity.Usuario;
import br.ce.clinica.enums.TipoUsuario;
import br.ce.clinica.exception.BadRequestBusinessException;
import br.ce.clinica.exception.UnauthorizedBusinessException;
import br.ce.clinica.repository.UsuarioRepository;
import br.ce.clinica.security.GenerateToken;
import br.ce.clinica.security.PasswordEnconder;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @InjectMock
    UsuarioRepository usuarioRepository;

    @InjectMock
    GenerateToken generateToken;

    @InjectMock
    PasswordEnconder passwordEncoder;

    @Inject
    AuthServiceImpl authService;

    private Usuario usuario;
    private LoginRequest loginRequest;
    private UsuarioRequest usuarioRequest;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .nome("João da Silva")
                .email("joao@email.com")
                .senha("hashedPassword123")
                .tipoUsuario(TipoUsuario.PSICOLOGO)
                .build();
        usuario.setId(1L);

        loginRequest = LoginRequest.builder()
                .email("joao@email.com")
                .senha("senha123")
                .build();

        usuarioRequest = UsuarioRequest.builder()
                .nome("João da Silva")
                .email("joao@email.com")
                .senha("senha123")
                .build();
    }

    @Test
    @DisplayName("Deve realizar login com sucesso")
    @RunOnVertxContext
    void loginComSucesso(UniAsserter asserter) {
        String expectedToken = "jwt.token.generated";

        asserter.execute(() -> {
            when(usuarioRepository.findByEmail(loginRequest.getEmail()))
                    .thenReturn(Uni.createFrom().item(usuario));
            when(passwordEncoder.matches(loginRequest.getSenha(), usuario.getSenha()))
                    .thenReturn(true);
            when(generateToken.generateToken(usuario))
                    .thenReturn(expectedToken);
        });

        asserter.assertThat(
            () -> authService.login(loginRequest),
            result -> {
                assertNotNull(result);
                assertEquals(expectedToken, result.getAccessToken());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedBusinessException quando usuário não existe")
    @RunOnVertxContext
    void loginFalhaUsuarioNaoEncontrado(UniAsserter asserter) {
        asserter.execute(() -> {
            when(usuarioRepository.findByEmail(loginRequest.getEmail()))
                    .thenReturn(Uni.createFrom().nullItem());
        });

        asserter.assertFailedWith(
            () -> authService.login(loginRequest),
            throwable -> {
                assertInstanceOf(UnauthorizedBusinessException.class, throwable);
                assertEquals("Usuário ou senha inválidos", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedBusinessException quando senha está incorreta")
    @RunOnVertxContext
    void loginFalhaSenhaIncorreta(UniAsserter asserter) {
        asserter.execute(() -> {
            when(usuarioRepository.findByEmail(loginRequest.getEmail()))
                    .thenReturn(Uni.createFrom().item(usuario));
            when(passwordEncoder.matches(loginRequest.getSenha(), usuario.getSenha()))
                    .thenReturn(false);
        });

        asserter.assertFailedWith(
            () -> authService.login(loginRequest),
            throwable -> {
                assertInstanceOf(UnauthorizedBusinessException.class, throwable);
                assertEquals("Usuário ou senha inválidos", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve salvar novo usuário com sucesso")
    @RunOnVertxContext
    void saveUsuarioComSucesso(UniAsserter asserter) {
        String hashedPassword = "hashedPassword123";

        asserter.execute(() -> {
            when(usuarioRepository.findByEmail(usuarioRequest.getEmail()))
                    .thenReturn(Uni.createFrom().nullItem());
            when(passwordEncoder.hash(usuarioRequest.getSenha()))
                    .thenReturn(hashedPassword);
            when(usuarioRepository.persist(any(Usuario.class)))
                    .thenReturn(Uni.createFrom().item(usuario));
        });

        asserter.assertThat(
            () -> authService.save(usuarioRequest),
            result -> {
                assertNotNull(result);
                assertEquals(usuario.getId(), result.getId());
                assertEquals(usuario.getNome(), result.getNome());
                assertEquals(usuario.getEmail(), result.getEmail());
                assertEquals(TipoUsuario.PSICOLOGO, result.getTipoUsuario());
            }
        );
    }

    @Test
    @DisplayName("Deve lançar BadRequestBusinessException quando email já está cadastrado")
    @RunOnVertxContext
    void saveFalhaEmailJaCadastrado(UniAsserter asserter) {
        asserter.execute(() -> {
            when(usuarioRepository.findByEmail(usuarioRequest.getEmail()))
                    .thenReturn(Uni.createFrom().item(usuario));
        });

        asserter.assertFailedWith(
            () -> authService.save(usuarioRequest),
            throwable -> {
                assertInstanceOf(BadRequestBusinessException.class, throwable);
                assertEquals("Email já cadastrado", throwable.getMessage());
            }
        );
    }

    @Test
    @DisplayName("Deve criar usuário com tipo PSICOLOGO por padrão")
    @RunOnVertxContext
    void saveUsuarioComTipoPsicologoPadrao(UniAsserter asserter) {
        String hashedPassword = "hashedPassword123";

        asserter.execute(() -> {
            when(usuarioRepository.findByEmail(usuarioRequest.getEmail()))
                    .thenReturn(Uni.createFrom().nullItem());
            when(passwordEncoder.hash(usuarioRequest.getSenha()))
                    .thenReturn(hashedPassword);
            when(usuarioRepository.persist(any(Usuario.class)))
                    .thenReturn(Uni.createFrom().item(usuario));
        });

        asserter.assertThat(
            () -> authService.save(usuarioRequest),
            result -> {
                assertNotNull(result);
                assertEquals(TipoUsuario.PSICOLOGO, result.getTipoUsuario());
            }
        );
    }

    @Test
    @DisplayName("Deve fazer hash da senha ao salvar usuário")
    @RunOnVertxContext
    void saveUsuarioComSenhaHasheada(UniAsserter asserter) {
        String senhaOriginal = "senha123";
        String hashedPassword = "hashedPassword123";

        usuarioRequest.setSenha(senhaOriginal);

        asserter.execute(() -> {
            when(usuarioRepository.findByEmail(usuarioRequest.getEmail()))
                    .thenReturn(Uni.createFrom().nullItem());
            when(passwordEncoder.hash(senhaOriginal))
                    .thenReturn(hashedPassword);
            when(usuarioRepository.persist(any(Usuario.class)))
                    .thenReturn(Uni.createFrom().item(usuario));
        });

        asserter.assertThat(
            () -> authService.save(usuarioRequest),
            result -> {
                assertNotNull(result);
                verify(passwordEncoder).hash(senhaOriginal);
            }
        );
    }

    @Test
    @DisplayName("Deve retornar token válido no login")
    @RunOnVertxContext
    void loginRetornaTokenValido(UniAsserter asserter) {
        String generatedToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.valid.token";

        asserter.execute(() -> {
            when(usuarioRepository.findByEmail(loginRequest.getEmail()))
                    .thenReturn(Uni.createFrom().item(usuario));
            when(passwordEncoder.matches(loginRequest.getSenha(), usuario.getSenha()))
                    .thenReturn(true);
            when(generateToken.generateToken(usuario))
                    .thenReturn(generatedToken);
        });

        asserter.assertThat(
            () -> authService.login(loginRequest),
            result -> {
                assertNotNull(result);
                assertNotNull(result.getAccessToken());
                assertFalse(result.getAccessToken().isEmpty());
            }
        );
    }

    @Test
    @DisplayName("Deve chamar generateToken com o usuário correto")
    @RunOnVertxContext
    void loginChamaGenerateTokenComUsuarioCorreto(UniAsserter asserter) {
        String expectedToken = "jwt.token";

        asserter.execute(() -> {
            when(usuarioRepository.findByEmail(loginRequest.getEmail()))
                    .thenReturn(Uni.createFrom().item(usuario));
            when(passwordEncoder.matches(loginRequest.getSenha(), usuario.getSenha()))
                    .thenReturn(true);
            when(generateToken.generateToken(usuario))
                    .thenReturn(expectedToken);
        });

        asserter.assertThat(
            () -> authService.login(loginRequest),
            result -> {
                assertNotNull(result);
                verify(generateToken).generateToken(usuario);
            }
        );
    }


    @Test
    @DisplayName("Deve persistir usuário com dados corretos")
    @RunOnVertxContext
    void saveUsuarioPersisteDadosCorretos(UniAsserter asserter) {
        String hashedPassword = "hashedPassword123";

        asserter.execute(() -> {
            when(usuarioRepository.findByEmail(usuarioRequest.getEmail()))
                    .thenReturn(Uni.createFrom().nullItem());
            when(passwordEncoder.hash(usuarioRequest.getSenha()))
                    .thenReturn(hashedPassword);
            when(usuarioRepository.persist(any(Usuario.class)))
                    .thenAnswer(invocation -> {
                        Usuario u = invocation.getArgument(0);
                        assertEquals(usuarioRequest.getNome(), u.getNome());
                        assertEquals(usuarioRequest.getEmail(), u.getEmail());
                        assertEquals(hashedPassword, u.getSenha());
                        assertEquals(TipoUsuario.PSICOLOGO, u.getTipoUsuario());
                        u.setId(1L);
                        return Uni.createFrom().item(u);
                    });
        });

        asserter.assertThat(
            () -> authService.save(usuarioRequest),
            result -> assertNotNull(result)
        );
    }
}

