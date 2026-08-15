package com.pedroteles.finvoice.dto;

import java.time.LocalDateTime;

public record AssistantResponse(
        String message,
        String understoodAction,
        LocalDateTime processedAt
) {
}
