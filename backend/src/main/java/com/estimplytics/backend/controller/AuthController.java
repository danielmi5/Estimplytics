package com.estimplytics.backend.controller;

import com.estimplytics.backend.dto.TokenRequestDTO;
import com.estimplytics.backend.dto.TokenResponseDTO;
import com.estimplytics.backend.entity.User;
import com.estimplytics.backend.repository.UserRepository;
import com.estimplytics.backend.dto.RefreshRequestDTO;
import com.estimplytics.backend.dto.UserResponseDTO;
import com.estimplytics.backend.security.JwtService;
import com.estimplytics.backend.security.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, CustomUserDetailsService userDetailsService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates the user and returns a JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody TokenRequestDTO request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid email or password", e);
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User userEntity = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String access = jwtService.generateToken(userDetails, userEntity.getName());
        String refresh = jwtService.generateRefreshToken(userDetails);

        return ResponseEntity.ok(TokenResponseDTO.builder()
            .accessToken(access)
            .refreshToken(refresh)
            .tokenType("Bearer")
            .expiresIn(jwtService.getAccessTokenSeconds())
            .user(UserResponseDTO.builder()
                .id(userEntity.getId())
                .name(userEntity.getName())
                .email(userEntity.getEmail())
                .role(userEntity.getRole() != null ? userEntity.getRole().name() : null)
                .createdAt(userEntity.getCreatedAt())
                .build())
            .build());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Exchanges a valid refresh token for a new access token")
    public ResponseEntity<TokenResponseDTO> refresh(@RequestBody @Valid RefreshRequestDTO request) {
        String refresh = request.getRefreshToken();
        String email;
        try {
            email = jwtService.extractUsername(refresh);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (email == null || !jwtService.isRefreshTokenValid(refresh)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        User userEntity = userRepository.findByEmail(email).orElseThrow();

        String access = jwtService.generateToken(userDetails, userEntity.getName());
        String newRefresh = jwtService.generateRefreshToken(userDetails);

        return ResponseEntity.ok(TokenResponseDTO.builder()
            .accessToken(access)
            .refreshToken(newRefresh)
            .tokenType("Bearer")
            .expiresIn(jwtService.getAccessTokenSeconds())
            .user(UserResponseDTO.builder()
                .id(userEntity.getId())
                .name(userEntity.getName())
                .email(userEntity.getEmail())
                .role(userEntity.getRole() != null ? userEntity.getRole().name() : null)
                .createdAt(userEntity.getCreatedAt())
                .build())
            .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        return ResponseEntity.noContent().build();
    }
}
