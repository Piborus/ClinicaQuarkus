package br.ce.clinica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TokenResponse {

    private String accessToken;
//    private String refreshToken;
//    private Long expiresIn;


   public static TokenResponse tokenResponse(String accessToken) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .build();
   }

}
