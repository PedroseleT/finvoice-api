package com.pedroteles.finvoice.controller;

import com.pedroteles.finvoice.dto.AssistantAudioResponse;
import com.pedroteles.finvoice.dto.AssistantResponse;
import com.pedroteles.finvoice.dto.AssistantTextRequest;
import com.pedroteles.finvoice.dto.SpeechRequest;
import com.pedroteles.finvoice.service.AssistantService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/assistant")
public class AssistantController {

    private static final MediaType AUDIO_MPEG = MediaType.parseMediaType("audio/mpeg");

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/text")
    public AssistantResponse processText(@Valid @RequestBody AssistantTextRequest request) {
        return assistantService.processText(request.message());
    }

    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AssistantAudioResponse processAudio(@RequestPart("file") MultipartFile file) {
        return assistantService.processAudio(file);
    }

    @PostMapping(value = "/speech", produces = "audio/mpeg")
    public ResponseEntity<byte[]> generateSpeech(@Valid @RequestBody SpeechRequest request) {
        return ResponseEntity
                .ok()
                .contentType(AUDIO_MPEG)
                .body(assistantService.generateSpeech(request.text()));
    }

    @PostMapping(value = "/audio/speech", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "audio/mpeg")
    public ResponseEntity<byte[]> processAudioToSpeech(@RequestPart("file") MultipartFile file) {
        return ResponseEntity
                .ok()
                .contentType(AUDIO_MPEG)
                .body(assistantService.processAudioToSpeech(file));
    }
}
