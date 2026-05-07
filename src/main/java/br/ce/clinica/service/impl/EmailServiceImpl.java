package br.ce.clinica.service.impl;

import br.ce.clinica.dto.request.LembreteDeConsultaRequest;
import br.ce.clinica.exception.NotFoundBusinessException;
import br.ce.clinica.exception.UnprocessableEntityBusinessException;
import br.ce.clinica.repository.UsuarioRepository;
import br.ce.clinica.security.CodigoRecuperacao;
import br.ce.clinica.service.EmailService;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.Location;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class EmailServiceImpl implements EmailService {

    private static final String CONTENT_ID = "<logo@intuitivopsi.com>";

    private static final String LOGO = "META-INF/microprofile-jwt/imagens/logotipo2.png";

    @Inject
    @Location("mail/lembreteConsulta")
    MailTemplate templateLembrete;

    @Inject
    @Location("mail/esqueciSenha")
    MailTemplate templateEsqueciSenha;

    @Inject
    CodigoRecuperacao codigoRecuperacao;

    @Inject
    ReactiveMailer mailer;

    @Inject
    UsuarioRepository usuarioRepository;

    @Override
    public Uni<String> enviarLembreConsulta(
            String destinatario,
            String nomePaciente,
            String nomeProfissional,
            String dataConsulta,
            String horaConsulta) {
        return templateLembrete.to(destinatario)
                .subject("Lembrete de Consulta")
                .data("nomePaciente", nomePaciente)
                .data("nomeProfissional", nomeProfissional)
                .data("dataConsulta", dataConsulta)
                .data("horaConsulta", horaConsulta)
                .send()
                .map(v -> "Lembrete enviado com sucesso para " + destinatario);
    }

    @Override
    public Uni<Void> mandarLembreConsulta(LembreteDeConsultaRequest request) {
        String html = templateLembrete
                .data("nomePaciente", request.getNomePaciente())
                .data("nomeProfissional", request.getNomeProfissional())
                .data("especialidade", request.getEspecialidade())
                .data("dataConsulta", request.getDataConsulta())
                .data("horaConsulta", request.getHoraConsulta())
                .templateInstance().render();

        File logo = carregarLogoTemporario();


        Mail mail = Mail.withHtml(request.getDestinatario(), "Lembrete de Consulta", html)
                .addInlineAttachment(
                        "logotipo.png",
                        logo,
                        "image/png",
                        CONTENT_ID
                );

        return mailer.send(mail).onFailure().invoke(erro ->
                System.err.println("Erro ao enviar e-mail: " + erro.getMessage())
        ).replaceWithVoid();
    }

    @Override
    public Uni<Void> esqueciSenha(String email) {
        return usuarioRepository.findByEmail(email)
                .onItem().ifNull()
                .failWith(() -> new NotFoundBusinessException(null))
                .onItem().ifNotNull()
                .transformToUni(usuario -> {
                    
                    String expiracao = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                    String html = templateEsqueciSenha
                            .data("usuario", usuario.getNome())
                            .data("codigoRecuperacao", codigoRecuperacao.gerarCodigoRecuperacao())
                            .data("dataExpiracao", expiracao)
                            .data("linkRecuperacao", "https://www.google.com/?hl=pt_BR")
                            .templateInstance().render();

                    Mail mail = Mail.withHtml(email, "Recuperação de Senha", html)
                            .addInlineAttachment(
                                    "logotipo.png",
                                    carregarLogoTemporario(),
                                    "image/png",
                                    CONTENT_ID
                            );

                    return mailer.send(mail)
                            .onFailure().invoke(erro ->
                                    new UnprocessableEntityBusinessException("Erro ao enviar e-mail: " + erro.getMessage())
                            ).replaceWithVoid();
                });
    }


    private File carregarLogoTemporario() {
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(LOGO)) {

            Path tempFile = Files.createTempFile("logotipo", ".png");
            assert inputStream != null;
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();
            return tempFile.toFile();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar logotipo do e-mail.", e);
        }
    }

}
