package com.estimplytics.backend.controller;

import com.estimplytics.backend.exception.RedmineIntegrationException;
import com.estimplytics.backend.service.RedmineIntegrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class RedmineIntegrationControllerTest {

    @Mock
    private RedmineIntegrationService service;

    @InjectMocks
    private RedmineIntegrationController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void syncIssues_whenSuccessful_shouldReturnOkWithCount() {
        when(service.syncIssuesFromRedmine(1L, false)).thenReturn(5);

        ResponseEntity<String> response = controller.syncIssues(1L, false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Synchronization successful. 5 records synchronized.", response.getBody());
    }

    @Test
    void syncIssues_whenNoRecordsSynced_shouldReturnOkWithZero() {
        when(service.syncIssuesFromRedmine(1L, false)).thenReturn(0);

        ResponseEntity<String> response = controller.syncIssues(1L, false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Synchronization successful. 0 records synchronized.", response.getBody());
    }

    @Test
    void syncIssues_whenExceptionThrown_shouldPropagateException() {
        when(service.syncIssuesFromRedmine(1L, false))
                .thenThrow(new RedmineIntegrationException("Connection timeout", new RuntimeException(), RedmineIntegrationException.ErrorType.NETWORK_ERROR));

        assertThrows(RedmineIntegrationException.class, () -> controller.syncIssues(1L, false));
    }
}
