package com.pedroteles.finvoice.ai;

import com.pedroteles.finvoice.exception.AiProviderNotConfiguredException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AiProviderStatus {

    private final String apiKey;

    public AiProviderStatus(@Value("${spring.ai.google.genai.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey) && !"not-configured".equals(apiKey);
    }

    public void requireConfigured() {
        if (!isConfigured()) {
            throw new AiProviderNotConfiguredException();
        }
    }
}
