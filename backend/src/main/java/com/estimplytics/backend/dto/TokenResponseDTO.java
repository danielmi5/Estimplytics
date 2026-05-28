package com.estimplytics.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponseDTO {
    @Schema(description = "Access JWT token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;
    @Schema(description = "Refresh JWT token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
    @Schema(description = "Token type", example = "Bearer")
    private String tokenType;
    @Schema(description = "Expiration time in milliseconds", example = "3600000")
    private long expiresIn;
    private UserResponseDTO user;
}
