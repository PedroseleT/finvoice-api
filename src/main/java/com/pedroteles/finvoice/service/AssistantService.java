package com.pedroteles.finvoice.service;

import com.pedroteles.finvoice.ai.AiProviderStatus;
import com.pedroteles.finvoice.ai.SpringAiClient;
import com.pedroteles.finvoice.dto.AssistantAudioResponse;
import com.pedroteles.finvoice.dto.AssistantResponse;
import com.pedroteles.finvoice.exception.AiProcessingException;
import com.pedroteles.finvoice.exception.InvalidAudioFileException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AssistantService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "audio/mpeg",
            "audio/mp3",
            "audio/mp4",
            "audio/wav",
            "audio/x-wav",
            "audio/webm",
            "audio/ogg"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".mp3",
            ".mp4",
            ".mpeg",
            ".mpga",
            ".m4a",
            ".wav",
            ".webm",
            ".ogg"
    );

    private final SpringAiClient springAiClient;
    private final AiProviderStatus aiProviderStatus;
    private final long maxAudioSizeBytes;

    public AssistantService(
            SpringAiClient springAiClient,
            AiProviderStatus aiProviderStatus,
            @Value("${finvoice.ai.audio.max-size-bytes}") long maxAudioSizeBytes
    ) {
        this.springAiClient = springAiClient;
        this.aiProviderStatus = aiProviderStatus;
        this.maxAudioSizeBytes = maxAudioSizeBytes;
    }

    public AssistantResponse processText(String message) {
        aiProviderStatus.requireConfigured();

        try {
            String response = springAiClient.chat(message);
            return new AssistantResponse(response, "Mensagem interpretada pelo modelo com Tool Calling.", LocalDateTime.now());
        } catch (RuntimeException exception) {
            throw new AiProcessingException("Não foi possível processar a mensagem com IA.", exception);
        }
    }

    public AssistantAudioResponse processAudio(MultipartFile file) {
        validateAudio(file);
        aiProviderStatus.requireConfigured();

        try {
            String transcription = springAiClient.transcribe(toResource(file));
            AssistantResponse response = processText(transcription);

            return new AssistantAudioResponse(
                    transcription,
                    response.message(),
                    response.understoodAction(),
                    response.processedAt()
            );
        } catch (IOException exception) {
            throw new InvalidAudioFileException("Não foi possível ler o arquivo de áudio.");
        } catch (RuntimeException exception) {
            throw new AiProcessingException("Não foi possível processar o áudio com IA.", exception);
        }
    }

    public byte[] generateSpeech(String text) {
        aiProviderStatus.requireConfigured();

        try {
            return springAiClient.speech(text);
        } catch (RuntimeException exception) {
            throw new AiProcessingException("Não foi possível gerar a resposta em áudio.", exception);
        }
    }

    public byte[] processAudioToSpeech(MultipartFile file) {
        AssistantAudioResponse response = processAudio(file);
        return generateSpeech(response.response());
    }

    private void validateAudio(MultipartFile file) {
        if (file == null) {
            throw new InvalidAudioFileException("Arquivo de áudio é obrigatório.");
        }

        if (file.isEmpty()) {
            throw new InvalidAudioFileException("Arquivo de áudio não pode estar vazio.");
        }

        if (file.getSize() > maxAudioSizeBytes) {
            throw new InvalidAudioFileException("Arquivo de áudio excede o tamanho máximo permitido.");
        }

        if (!hasSupportedFormat(file)) {
            throw new InvalidAudioFileException("Formato de áudio não suportado.");
        }
    }

    private boolean hasSupportedFormat(MultipartFile file) {
        String contentType = file.getContentType();

        if (StringUtils.hasText(contentType) && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            return true;
        }

        String filename = file.getOriginalFilename();

        if (!StringUtils.hasText(filename)) {
            return false;
        }

        String lowerFilename = filename.toLowerCase(Locale.ROOT);

        return ALLOWED_EXTENSIONS.stream().anyMatch(lowerFilename::endsWith);
    }

    private ByteArrayResource toResource(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();

        return new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }
}
