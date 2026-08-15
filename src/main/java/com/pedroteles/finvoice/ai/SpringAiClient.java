package com.pedroteles.finvoice.ai;

import com.pedroteles.finvoice.tool.FinancialTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;

@Component
public class SpringAiClient {

    private static final String SYSTEM_PROMPT = """
            Você é um assistente financeiro pessoal em português do Brasil.
            Interprete comandos de texto e escolha as ferramentas financeiras quando precisar criar ou consultar transações.
            Use somente as categorias FOOD, TRANSPORT, HEALTH, EDUCATION, LEISURE, SALARY, SHOPPING ou OTHER.
            Para alimentação, mercado, restaurante e similares use FOOD.
            Para salário e recebimentos de trabalho use SALARY.
            Se faltar valor, descrição ou categoria, peça a informação em uma frase curta.
            Responda de forma natural, curta e objetiva.
            """;

    private final ChatClient chatClient;
    private final FinancialTools financialTools;

    public SpringAiClient(
            ChatClient chatClient,
            FinancialTools financialTools
    ) {
        this.chatClient = chatClient;
        this.financialTools = financialTools;
    }

    public String chat(String message) {
        String response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(message)
                .tools(financialTools)
                .call()
                .content();

        if (!StringUtils.hasText(response)) {
            return "Não consegui interpretar esse comando financeiro.";
        }

        return response;
    }

    public String chatWithAudio(Resource audio, MimeType mimeType) {
        String response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(user -> user
                        .text("Interprete o áudio como um comando financeiro em português do Brasil e execute a ferramenta adequada quando necessário.")
                        .media(mimeType, audio))
                .tools(financialTools)
                .call()
                .content();

        if (!StringUtils.hasText(response)) {
            return "Não consegui interpretar esse áudio financeiro.";
        }

        return response;
    }
}
