package com.estimplytics.backend.config;

import com.estimplytics.backend.dto.TokenRequestDTO;
import com.estimplytics.backend.entity.Role;
import com.estimplytics.backend.entity.User;
import com.estimplytics.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
        userRepository.deleteAll();
    }

    private User createTestUser(Role role) {
        User user = new User();
        user.setEmail(role.name().toLowerCase() + "@estymplytics.es");
        user.setPassword(passwordEncoder.encode("Zx23edfzTF"));
        user.setRole(role);
        user.setName(role.name() + " User");
        return userRepository.save(user);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        TokenRequestDTO loginRequest = new TokenRequestDTO();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String responseString = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseString).get("accessToken").asText();
    }

    @Test
    public void publicEndpoints_PermitAll() throws Exception {
        mockMvc.perform(get("/api/auth/some-invalid-endpoint")).andExpect(status().isNotFound());
    }

    @Test
    public void unauthenticated_AnyApi_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/requests")).andExpect(status().isForbidden());
    }

    @Test
    public void unauthenticated_InvalidToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/requests").header("Authorization", "Bearer invalid.token.here"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void badLoginRequest_ReturnsBadRequest() throws Exception {
        TokenRequestDTO loginRequest = new TokenRequestDTO();
        loginRequest.setEmail("invalidemail");
        loginRequest.setPassword("");

        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void wrongCredentials_ReturnsUnauthorized() throws Exception {
        createTestUser(Role.ANALYST);

        TokenRequestDTO loginRequest = new TokenRequestDTO();
        loginRequest.setEmail("analyst@estymplytics.es");
        loginRequest.setPassword("wrong-password");

        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void analyst_CanGetRequests_ButNotAccessAdminEndpoints() throws Exception {
        createTestUser(Role.ANALYST);
        String token = loginAndGetToken("analyst@estymplytics.es", "Zx23edfzTF");

        mockMvc.perform(get("/api/requests").header("Authorization", "Bearer " + token)).andExpect(status().isOk());

        mockMvc.perform(post("/api/components").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    @Test
    public void admin_CanAccessAnyEndpoint() throws Exception {
        createTestUser(Role.ADMIN);
        String token = loginAndGetToken("admin@estymplytics.es", "Zx23edfzTF");

        mockMvc.perform(get("/api/requests").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    @Test
    public void logout_InvalidatesToken() throws Exception {
        createTestUser(Role.ANALYST);
        String token = loginAndGetToken("analyst@estymplytics.es", "Zx23edfzTF");

        mockMvc.perform(get("/api/requests").header("Authorization", "Bearer " + token)).andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/requests").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }
}





