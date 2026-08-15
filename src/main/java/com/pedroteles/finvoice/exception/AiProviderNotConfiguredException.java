package com.pedroteles.finvoice.exception;

public class AiProviderNotConfiguredException extends RuntimeException {

    public AiProviderNotConfiguredException() {
        super("OPENAI_API_KEY não está configurada para os endpoints de IA.");
    }
}
