package com.pedroteles.finvoice.dto;

import jakarta.validation.constraints.NotBlank;

public record AssistantTextRequest(
        @NotBlank String message
) {
}
