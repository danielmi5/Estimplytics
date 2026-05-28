package com.estimplytics.backend.dto.redmine;

import io.swagger.v3.oas.annotations.media.Schema;

public record RedmineCredentialDTO(
        @Schema(description = "Redmine base URL", example = "https://redmine.mycompany.com")
        String redmineUrl,

        @Schema(description = "Plain Redmine API key (leave blank for public instances)", example = "your-redmine-api-key")
        String plainApiKey
) {}
