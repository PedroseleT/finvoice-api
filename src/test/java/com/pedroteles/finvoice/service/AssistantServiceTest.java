package com.pedroteles.finvoice.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pedroteles.finvoice.ai.AiProviderStatus;
import com.pedroteles.finvoice.ai.SpringAiClient;
import com.pedroteles.finvoice.dto.AssistantAudioResponse;
import com.pedroteles.finvoice.dto.AssistantResponse;
import com.pedroteles.finvoice.exception.AiProviderNotConfiguredException;
import com.pedroteles.finvoice.exception.InvalidAudioFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class AssistantServiceTest {

    @Mock
    private SpringAiClient springAiClient;

    @Mock
    private AiProviderStatus aiProviderStatus;

    private AssistantService assistantService;

    @BeforeEach
    void setUp() {
        assistantService = new AssistantService(springAiClient, aiProviderStatus, 1024);
    }

    @Test
    void shouldProcessTextWithAiClient() {
        when(springAiClient.chat("Gastei 45 reais no mercado")).thenReturn("Despesa registrada.");

        AssistantResponse response = assistantService.processText("Gastei 45 reais no mercado");

        assertEquals("Despesa registrada.", response.message());
        verify(aiProviderStatus).requireConfigured();
        verify(springAiClient).chat("Gastei 45 reais no mercado");
    }

    @Test
    void shouldProcessAudioWithTranscriptionAndChat() {
        MockMultipartFile file = new MockMultipartFile("file", "audio.mp3", "audio/mpeg", new byte[]{1, 2, 3});

        when(springAiClient.transcribe(any(Resource.class))).thenReturn("Recebi 100 reais");
        when(springAiClient.chat("Recebi 100 reais")).thenReturn("Receita registrada.");

        AssistantAudioResponse response = assistantService.processAudio(file);

        assertEquals("Recebi 100 reais", response.transcription());
        assertEquals("Receita registrada.", response.response());
    }

    @Test
    void shouldRejectEmptyAudioFile() {
        MockMultipartFile file = new MockMultipartFile("file", "audio.mp3", "audio/mpeg", new byte[0]);

        assertThrows(InvalidAudioFileException.class, () -> assistantService.processAudio(file));
    }

    @Test
    void shouldRejectUnsupportedAudioFile() {
        MockMultipartFile file = new MockMultipartFile("file", "audio.txt", "text/plain", new byte[]{1, 2, 3});

        assertThrows(InvalidAudioFileException.class, () -> assistantService.processAudio(file));
    }

    @Test
    void shouldThrowWhenProviderIsNotConfigured() {
        doThrow(new AiProviderNotConfiguredException()).when(aiProviderStatus).requireConfigured();

        assertThrows(AiProviderNotConfiguredException.class, () -> assistantService.processText("Qual é meu saldo?"));
    }

    @Test
    void shouldGenerateSpeech() {
        byte[] audio = new byte[]{10, 20, 30};

        when(springAiClient.speech("Saldo atual")).thenReturn(audio);

        byte[] result = assistantService.generateSpeech("Saldo atual");

        assertArrayEquals(audio, result);
    }
}
