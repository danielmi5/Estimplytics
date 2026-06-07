package com.estimplytics.backend.controller;

import com.estimplytics.backend.dto.TokenRequestDTO;
import com.estimplytics.backend.dto.TokenResponseDTO;
import com.estimplytics.backend.entity.Role;
import com.estimplytics.backend.repository.UserRepository;
import com.estimplytics.backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;



    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthController authController;


    @Test
    void login_shouldThrowBadCredentialsWhenAuthenticationFails() {
        TokenRequestDTO request = new TokenRequestDTO();
        request.setEmail("daniel@test.com");
        request.setPassword("nopassword");
        AuthenticationException exception = mock(AuthenticationException.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(exception);

        assertThatThrownBy(() -> authController.login(request)).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_shouldReturnTokenWhenCredentialsAreValid_includesRefreshAndUser() {
        TokenRequestDTO request = new TokenRequestDTO();
        request.setEmail("user@test.com");
        request.setPassword("secret");

        UserDetails userDetails = User.withUsername("user@test.com").password("admin123").authorities("ROLE_USER").build();
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(
                com.estimplytics.backend.entity.User.builder()
                        .id(UUID.randomUUID())
                        .email("user@test.com")
                        .name("Daniel")
                        .role(Role.ANALYST)
                        .password("admin123")
                        .build()
        ));
        when(jwtService.generateToken(userDetails, "Daniel")).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh-token");
        when(jwtService.getAccessTokenSeconds()).thenReturn(3600L);

        ResponseEntity<TokenResponseDTO> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getBody().getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getBody().getTokenType()).isEqualTo("Bearer");
        assertThat(response.getBody().getExpiresIn()).isEqualTo(3600L);
        assertThat(response.getBody().getUser()).isNotNull();
    }
}
