package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.LoginRequest;
import br.ce.clinica.dto.request.RefreshTokenRequest;
import br.ce.clinica.dto.request.UsuarioRequest;
import br.ce.clinica.entity.Usuario;
import br.ce.clinica.enums.TipoUsuario;
import br.ce.clinica.exception.BadRequestBusinessException;
import br.ce.clinica.exception.UnauthorizedBusinessException;
import br.ce.clinica.repository.UsuarioRepository;
import br.ce.clinica.security.GenerateToken;
import br.ce.clinica.security.PasswordEnconder;
import br.ce.clinica.security.RefreshToken;
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

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    GenerateToken generateToken;

    @Mock
    PasswordEnconder passwordEncoder;

    @Mock
    RefreshToken refreshToken;

    @InjectMocks
    AuthServiceImpl authService;

    private MockedStatic<Panache> panacheMock;
    private Usuario usuario;
    private LoginRequest loginRequest;
    private UsuarioRequest usuarioRequest;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        panacheMock = mockStatic(Panache.class);
        panacheMock.when(() -> Panache.withTransaction(any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<Uni<?>>) invocation.getArgument(0)).get());

        usuario = Usuario.builder()
                .nome("Joao da Silva")
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
                .nome("Joao da Silva")
                .email("joao@email.com")
                .senha("senha123")
                .build();
    }

    @AfterEach
    void tearDown() {
        panacheMock.close();
    }

    @Test
    @DisplayName("Deve realizar login com sucesso")
    void loginComSucesso() {
        when(usuarioRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Uni.createFrom().item(usuario));
        when(passwordEncoder.matches(loginRequest.getSenha(), usuario.getSenha()))
                .thenReturn(true);
        when(generateToken.generateToken(usuario))
                .thenReturn("jwt.token.generated");
        when(refreshToken.generateRefreshToken(usuario.getId()))
                .thenReturn(Uni.createFrom().item("refresh.token.generated"));

        var result = authService.login(loginRequest).await().indefinitely();

        assertNotNull(result);
        assertEquals("jwt.token.generated", result.getAccessToken());
        assertEquals("refresh.token.generated", result.getRefreshToken());
    }

    @Test
    @DisplayName("Deve lancar UnauthorizedBusinessException quando usuario nao existe")
    void loginFalhaUsuarioNaoEncontrado() {
        when(usuarioRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Uni.createFrom().nullItem());

        var throwable = assertThrows(UnauthorizedBusinessException.class,
                () -> authService.login(loginRequest).await().indefinitely());

        assertEquals("Usuário ou senha inválidos", throwable.getMessage());
    }

    @Test
    @DisplayName("Deve lancar UnauthorizedBusinessException quando senha esta incorreta")
    void loginFalhaSenhaIncorreta() {
        when(usuarioRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Uni.createFrom().item(usuario));
        when(passwordEncoder.matches(loginRequest.getSenha(), usuario.getSenha()))
                .thenReturn(false);

        var throwable = assertThrows(UnauthorizedBusinessException.class,
                () -> authService.login(loginRequest).await().indefinitely());

        assertEquals("Usuário ou senha inválidos", throwable.getMessage());
    }

    @Test
    @DisplayName("Deve salvar novo usuario com sucesso")
    void saveUsuarioComSucesso() {
        when(usuarioRepository.findByEmail(usuarioRequest.getEmail()))
                .thenReturn(Uni.createFrom().nullItem());
        when(passwordEncoder.hash(usuarioRequest.getSenha()))
                .thenReturn("hashedPassword123");
        when(usuarioRepository.persist(any(Usuario.class)))
                .thenReturn(Uni.createFrom().item(usuario));

        var result = authService.save(usuarioRequest).await().indefinitely();

        assertNotNull(result);
        assertEquals(usuario.getId(), result.getId());
        assertEquals(usuario.getNome(), result.getNome());
        assertEquals(usuario.getEmail(), result.getEmail());
        assertEquals(TipoUsuario.PSICOLOGO, result.getTipoUsuario());
    }

    @Test
    @DisplayName("Deve lancar BadRequestBusinessException quando email ja esta cadastrado")
    void saveFalhaEmailJaCadastrado() {
        when(usuarioRepository.findByEmail(usuarioRequest.getEmail()))
                .thenReturn(Uni.createFrom().item(usuario));

        var throwable = assertThrows(BadRequestBusinessException.class,
                () -> authService.save(usuarioRequest).await().indefinitely());

        assertEquals("Email já cadastrado", throwable.getMessage());
    }

    @Test
    @DisplayName("Deve criar usuario com tipo psicologo por padrao")
    void saveUsuarioComTipoPsicologoPadrao() {
        when(usuarioRepository.findByEmail(usuarioRequest.getEmail()))
                .thenReturn(Uni.createFrom().nullItem());
        when(passwordEncoder.hash(usuarioRequest.getSenha()))
                .thenReturn("hashedPassword123");
        when(usuarioRepository.persist(any(Usuario.class)))
                .thenReturn(Uni.createFrom().item(usuario));

        var result = authService.save(usuarioRequest).await().indefinitely();

        assertEquals(TipoUsuario.PSICOLOGO, result.getTipoUsuario());
    }

    @Test
    @DisplayName("Deve fazer hash da senha ao salvar usuario")
    void saveUsuarioComSenhaHasheada() {
        when(usuarioRepository.findByEmail(usuarioRequest.getEmail()))
                .thenReturn(Uni.createFrom().nullItem());
        when(passwordEncoder.hash(usuarioRequest.getSenha()))
                .thenReturn("hashedPassword123");
        when(usuarioRepository.persist(any(Usuario.class)))
                .thenReturn(Uni.createFrom().item(usuario));

        var result = authService.save(usuarioRequest).await().indefinitely();

        assertNotNull(result);
        verify(passwordEncoder).hash("senha123");
    }

    @Test
    @DisplayName("Deve retornar token valido no login")
    void loginRetornaTokenValido() {
        when(usuarioRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Uni.createFrom().item(usuario));
        when(passwordEncoder.matches(loginRequest.getSenha(), usuario.getSenha()))
                .thenReturn(true);
        when(generateToken.generateToken(usuario))
                .thenReturn("valid.token");
        when(refreshToken.generateRefreshToken(usuario.getId()))
                .thenReturn(Uni.createFrom().item("refresh.token"));

        var result = authService.login(loginRequest).await().indefinitely();

        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertFalse(result.getAccessToken().isEmpty());
    }

    @Test
    @DisplayName("Deve chamar generateToken com o usuario correto")
    void loginChamaGenerateTokenComUsuarioCorreto() {
        when(usuarioRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Uni.createFrom().item(usuario));
        when(passwordEncoder.matches(loginRequest.getSenha(), usuario.getSenha()))
                .thenReturn(true);
        when(generateToken.generateToken(usuario))
                .thenReturn("jwt.token");
        when(refreshToken.generateRefreshToken(usuario.getId()))
                .thenReturn(Uni.createFrom().item("refresh.token"));

        authService.login(loginRequest).await().indefinitely();

        verify(generateToken).generateToken(usuario);
    }

    @Test
    @DisplayName("Deve persistir usuario com dados corretos")
    void saveUsuarioPersisteDadosCorretos() {
        when(usuarioRepository.findByEmail(usuarioRequest.getEmail()))
                .thenReturn(Uni.createFrom().nullItem());
        when(passwordEncoder.hash(usuarioRequest.getSenha()))
                .thenReturn("hashedPassword123");
        when(usuarioRepository.persist(any(Usuario.class)))
                .thenAnswer(invocation -> {
                    Usuario persisted = invocation.getArgument(0);
                    assertEquals(usuarioRequest.getNome(), persisted.getNome());
                    assertEquals(usuarioRequest.getEmail(), persisted.getEmail());
                    assertEquals("hashedPassword123", persisted.getSenha());
                    assertEquals(TipoUsuario.PSICOLOGO, persisted.getTipoUsuario());
                    persisted.setId(1L);
                    return Uni.createFrom().item(persisted);
                });

        var result = authService.save(usuarioRequest).await().indefinitely();

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Deve renovar access token com refresh token valido")
    void refreshTokenComSucesso() {
        when(refreshToken.validateRefreshToken("refresh.token"))
                .thenReturn(Uni.createFrom().item(usuario.getId()));
        when(usuarioRepository.findById(usuario.getId()))
                .thenReturn(Uni.createFrom().item(usuario));
        when(generateToken.generateToken(usuario))
                .thenReturn("new.access.token");

        var result = authService.refreshToken("refresh.token").await().indefinitely();

        assertEquals("new.access.token", result.getAccessToken());
        assertEquals("refresh.token", result.getRefreshToken());
    }

    @Test
    @DisplayName("Deve falhar ao renovar token quando usuario nao existe")
    void refreshTokenFalhaQuandoUsuarioNaoExiste() {
        when(refreshToken.validateRefreshToken("refresh.token"))
                .thenReturn(Uni.createFrom().item(usuario.getId()));
        when(usuarioRepository.findById(usuario.getId()))
                .thenReturn(Uni.createFrom().nullItem());

        var throwable = assertThrows(UnauthorizedBusinessException.class,
                () -> authService.refreshToken("refresh.token").await().indefinitely());

        assertEquals("Usuário não encontrado", throwable.getMessage());
    }

    @Test
    @DisplayName("Deve revogar refresh token no logout")
    void logoutComSucesso() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .token("refresh.token")
                .build();
        when(refreshToken.revokeRefreshToken(request.getToken()))
                .thenReturn(Uni.createFrom().voidItem());

        authService.logout(request).await().indefinitely();

        verify(refreshToken).revokeRefreshToken("refresh.token");
    }
}
