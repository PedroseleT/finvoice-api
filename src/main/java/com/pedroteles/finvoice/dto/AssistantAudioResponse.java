package com.pedroteles.finvoice.dto;

import java.time.LocalDateTime;

public record AssistantAudioResponse(
        String transcription,
        String response,
        String understoodAction,
        LocalDateTime processedAt
) {
}
