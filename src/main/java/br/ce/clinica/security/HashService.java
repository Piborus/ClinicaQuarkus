package br.ce.clinica.security;

import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@ApplicationScoped
public class HashService {

    public String hash(String valor) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash");
        }
    }

    public boolean verify(String valorInformado, String hashArmazenado) {
        if (valorInformado == null || hashArmazenado == null) {
            return false;
        }

        String hashInformado = hash(valorInformado);

        return MessageDigest.isEqual(
                hashInformado.getBytes(StandardCharsets.UTF_8),
                hashArmazenado.getBytes(StandardCharsets.UTF_8)
        );
    }


}
