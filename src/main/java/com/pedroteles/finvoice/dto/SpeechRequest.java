package com.pedroteles.finvoice.dto;

import jakarta.validation.constraints.NotBlank;

public record SpeechRequest(
        @NotBlank String text
) {
}
