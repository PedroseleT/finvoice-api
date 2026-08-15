package com.pedroteles.finvoice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pedroteles.finvoice.dto.AssistantAudioResponse;
import com.pedroteles.finvoice.dto.AssistantResponse;
import com.pedroteles.finvoice.dto.AssistantTextRequest;
import com.pedroteles.finvoice.dto.SpeechRequest;
import com.pedroteles.finvoice.exception.UnsupportedAiCapabilityException;
import com.pedroteles.finvoice.service.AssistantService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(AssistantController.class)
class AssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssistantService assistantService;

    @Test
    void shouldProcessTextCommand() throws Exception {
        AssistantTextRequest request = new AssistantTextRequest("Qual e meu saldo?");
        AssistantResponse response = new AssistantResponse(
                "Seu saldo atual e R$ 100.00.",
                "Consulta de saldo",
                LocalDateTime.now()
        );

        when(assistantService.processText("Qual e meu saldo?")).thenReturn(response);

        mockMvc.perform(post("/assistant/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Seu saldo atual e R$ 100.00."))
                .andExpect(jsonPath("$.understoodAction").value("Consulta de saldo"));
    }

    @Test
    void shouldProcessAudioCommand() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "audio.mp3", "audio/mpeg", new byte[]{1, 2, 3});
        AssistantAudioResponse response = new AssistantAudioResponse(
                null,
                "Despesa registrada.",
                "Criacao de despesa",
                LocalDateTime.now()
        );

        when(assistantService.processAudio(any(MultipartFile.class))).thenReturn(response);

        mockMvc.perform(multipart("/assistant/audio").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transcription").doesNotExist())
                .andExpect(jsonPath("$.response").value("Despesa registrada."));
    }

    @Test
    void shouldReturnNotImplementedForSpeech() throws Exception {
        SpeechRequest request = new SpeechRequest("Saldo atual");

        when(assistantService.generateSpeech("Saldo atual")).thenThrow(new UnsupportedAiCapabilityException("TTS nao suportado."));

        mockMvc.perform(post("/assistant/speech")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.status").value(501));
    }

    @Test
    void shouldRejectBlankTextCommand() throws Exception {
        String body = """
                {
                  "message": ""
                }
                """;

        mockMvc.perform(post("/assistant/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
