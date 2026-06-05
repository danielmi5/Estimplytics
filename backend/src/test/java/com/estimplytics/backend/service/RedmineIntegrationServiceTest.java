package com.estimplytics.backend.service;

import com.estimplytics.backend.entity.RedmineInstance;
import com.estimplytics.backend.entity.UserRedmineCredential;
import com.estimplytics.backend.repository.UserRedmineCredentialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class RedmineIntegrationServiceTest {

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private UserRedmineCredentialRepository userRedmineCredentialRepository;

    @Mock
    private RedmineIssuePersistenceService redmineIssuePersistenceService;

    @Mock
    private OwnershipService ownershipService;

    private RedmineIntegrationService service;

    @BeforeEach
    void setUp() {
        when(restClientBuilder.build()).thenReturn(restClient);
        service = new RedmineIntegrationService(
                restClientBuilder, userRedmineCredentialRepository, redmineIssuePersistenceService, ownershipService);
    }

    @Test
    void testConnection_shouldReturnConnected_whenServerRespondsOk() {
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        String result = service.testConnection(buildCredential("https://redmine.example.com", null));

        assertThat(result).isEqualTo("connected");
    }

    @Test
    void testConnection_shouldReturnConnected_whenApiKeyProvided() {
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(uriSpec);
        when(uriSpec.header(eq("X-Redmine-API-Key"), anyString())).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        String result = service.testConnection(buildCredential("https://redmine.example.com", "secret-key"));

        assertThat(result).isEqualTo("connected");
    }

    @Test
    void testConnection_shouldReturnUnauthorized_whenServer401() {
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null));

        String result = service.testConnection(buildCredential("https://redmine.example.com", null));

        assertThat(result).isEqualTo("unauthorized");
    }

    @Test
    void testConnection_shouldReturnUnauthorized_whenServer403() {
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(HttpClientErrorException.create(
                HttpStatus.FORBIDDEN, "Forbidden", null, null, null));

        String result = service.testConnection(buildCredential("https://redmine.example.com", null));

        assertThat(result).isEqualTo("unauthorized");
    }

    @Test
    void testConnection_shouldReturnUnreachable_whenNetworkError() {
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(new ResourceAccessException("Connection refused"));

        String result = service.testConnection(buildCredential("https://unreachable.invalid", null));

        assertThat(result).isEqualTo("unreachable");
    }

    @Test
    void testConnection_shouldReturnUnreachable_whenServer5xx() {
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(HttpClientErrorException.create(
                HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", null, null, null));

        String result = service.testConnection(buildCredential("https://redmine.example.com", null));

        assertThat(result).isEqualTo("unreachable");
    }

    @Test
    void testConnection_shouldStripTrailingSlashFromBaseUrl() {
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri("https://redmine.example.com/issues.json?limit=1")).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        String result = service.testConnection(buildCredential("https://redmine.example.com/", null));

        assertThat(result).isEqualTo("connected");
    }

    private UserRedmineCredential buildCredential(String baseUrl, String apiKey) {
        RedmineInstance instance = new RedmineInstance();
        instance.setBaseUrl(baseUrl);
        instance.setName(baseUrl);

        UserRedmineCredential credential = new UserRedmineCredential();
        credential.setRedmineInstance(instance);
        credential.setApiKey(apiKey);
        return credential;
    }
}
