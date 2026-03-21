package com.basiccrud.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponseDTO {

    private String token;
    private String type;
    private String email;
    private String name;

    public static AuthResponseDTO of(String token, String email, String name) {
        return AuthResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .email(email)
                .name(name)
                .build();
    }
}

