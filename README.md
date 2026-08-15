# FinVoice API

Assistente financeiro por voz desenvolvido com Java, Spring Boot e Spring AI.

![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring AI](https://img.shields.io/badge/Spring%20AI-1.1-6DB33F?style=for-the-badge)
![CI](https://img.shields.io/github/actions/workflow/status/PedroseleT/finvoice-api/ci.yml?branch=main&style=for-the-badge&label=CI)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)

## Sobre o projeto

FinVoice API é uma API financeira que permite cadastrar e consultar transações pessoais usando endpoints REST tradicionais e comandos interpretados por IA.

A ideia principal é transformar comandos como "gastei 45 reais no mercado" ou "qual é meu saldo atual?" em operações reais da aplicação. O Spring AI interpreta a intenção, escolhe uma ferramenta com Tool Calling e a regra de negócio é executada pelo mesmo serviço usado pelos endpoints REST.

Os dados são armazenados em H2 em memória. No ambiente demonstrativo, as informações são temporárias e podem ser perdidas quando o serviço reinicia.

## Desafio

O projeto foi desenvolvido para o desafio de Spring Boot com Spring AI da DIO, aplicando os conceitos de ChatClient, Tool Calling, transcrição de áudio e geração de fala em uma API original de finanças pessoais.

## Minha evolução

- Comandos financeiros por texto
- Comandos financeiros por áudio
- Consulta de saldo
- Resumo financeiro geral
- Resumo por categoria
- Validações antes de salvar transações
- Resposta estruturada com o que foi interpretado
- Testes automatizados sem depender de chamadas externas

## Tecnologias

| Tecnologia | Uso |
| --- | --- |
| Java 21 | Linguagem principal |
| Spring Boot | Base da API |
| Spring AI | Integração com IA |
| ChatClient | Interpretação dos comandos |
| Tool Calling | Execução das ações financeiras |
| TranscriptionModel | Transcrição de áudio |
| TextToSpeechModel | Geração de voz |
| Spring Data JPA | Persistência |
| H2 Database | Banco em memória |
| Bean Validation | Validação dos DTOs |
| SpringDoc OpenAPI | Swagger |
| JUnit 5 | Testes |
| Mockito | Mocks |
| Docker | Container da aplicação |
| GitHub Actions | CI |
| Render | Deploy |

## Como a IA funciona

```mermaid
flowchart LR
    A[Audio ou Text] --> B{Entrada de áudio?}
    B -->|Sim| C[TranscriptionModel]
    B -->|Não| D[ChatClient]
    C --> D
    D --> E[Tool Calling]
    E --> F[FinancialTools]
    F --> G[TransactionService]
    G --> H[Repository]
    H --> I[Response]
    I --> J{Gerar voz?}
    J -->|Sim| K[TextToSpeechModel]
    J -->|Não| L[JSON]
    K --> M[audio/mpeg]
```

A transcrição só acontece no fluxo de áudio. Para comandos em texto, a mensagem vai direto para o ChatClient.

## Tool Calling

| Tool | O que faz |
| --- | --- |
| createExpense | Registra uma despesa |
| createIncome | Registra uma receita |
| getBalance | Consulta o saldo atual |
| getSummary | Consulta o resumo financeiro geral |
| getSummaryByCategory | Consulta o resumo por categoria |
| getRecentTransactions | Lista as últimas transações |

## Fluxo de exemplo

Comando:

```text
Gastei R$ 45 no mercado
```

A IA identifica:

| Campo | Valor |
| --- | --- |
| type | EXPENSE |
| amount | 45 |
| category | FOOD |
| description | mercado |

Depois disso, o Tool Calling aciona `createExpense`, a aplicação salva a transação e o usuário recebe uma confirmação curta.

## Endpoints

| Método | Endpoint | Descrição |
| --- | --- | --- |
| GET | `/actuator/health` | Health check |
| POST | `/transactions` | Cria uma transação manualmente |
| GET | `/transactions` | Lista transações |
| GET | `/transactions/{id}` | Busca transação por ID |
| DELETE | `/transactions/{id}` | Remove uma transação |
| GET | `/transactions/balance` | Consulta saldo |
| GET | `/transactions/summary` | Consulta resumo financeiro |
| GET | `/transactions/summary?category=FOOD` | Consulta resumo por categoria |
| POST | `/assistant/text` | Processa comando por texto |
| POST | `/assistant/audio` | Processa comando por áudio |
| POST | `/assistant/speech` | Gera fala a partir de texto |
| POST | `/assistant/audio/speech` | Processa áudio e retorna resposta em voz |

## Exemplos

Criar transação manual:

```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d "{\"description\":\"Mercado\",\"amount\":45.00,\"type\":\"EXPENSE\",\"category\":\"FOOD\"}"
```

Enviar comando por texto:

```bash
curl -X POST http://localhost:8080/assistant/text \
  -H "Content-Type: application/json" \
  -d "{\"message\":\"Gastei 45 reais no mercado\"}"
```

Consultar saldo:

```bash
curl http://localhost:8080/transactions/balance
```

Consultar resumo:

```bash
curl http://localhost:8080/transactions/summary
```

## Áudio

O endpoint `/assistant/audio` recebe `multipart/form-data` com o campo `file`.

Formatos documentados para demonstração:

- mp3
- mp4
- mpeg
- mpga
- m4a
- wav
- webm
- ogg

O arquivo precisa existir, não pode estar vazio e deve respeitar o limite de tamanho configurado.

## Configuração da IA

Os endpoints REST financeiros, Swagger, health check, testes e build funcionam sem chave de IA.

Para usar os endpoints com Spring AI, configure a variável de ambiente:

```env
OPENAI_API_KEY=sua_chave_aqui
```

Sem essa variável, os endpoints de IA retornam uma mensagem clara informando que o provider não está configurado.

## Executando localmente

Windows:

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw test
./mvnw clean package
./mvnw spring-boot:run
```

## Swagger

Local:

```text
http://localhost:8080/swagger-ui.html
```

Produção:

```text
https://finvoice-api-s0ah.onrender.com/swagger-ui.html
```

## Testes

Os testes cobrem:

- criação de transações
- validações de entrada
- busca por ID
- remoção
- saldo
- resumo financeiro
- resumo por categoria
- tools financeiras
- AssistantService com dependências de IA mockadas
- controllers REST
- multipart de áudio
- geração de resposta em áudio

Comando:

```bash
./mvnw test
```

## Deploy

Aplicação:

```text
https://finvoice-api-s0ah.onrender.com
```

Swagger:

```text
https://finvoice-api-s0ah.onrender.com/swagger-ui.html
```

O deploy utiliza Docker e H2 em memória, sem banco externo.

## Docker

Build da imagem:

```bash
docker build -t finvoice-api .
```

Executar container:

```bash
docker run -p 8080:8080 finvoice-api
```

## Estrutura

```text
com.pedroteles.finvoice
├── ai
├── controller
├── dto
├── entity
├── enums
├── exception
├── repository
├── service
└── tool
```

## Segurança

A chave da OpenAI é lida somente por variável de ambiente e não é versionada. Os testes automatizados não realizam chamadas reais para APIs externas e o CI não depende de credenciais.

## Aprendizados

Este projeto reforçou como integrar IA a uma aplicação com regras reais, mantendo as responsabilidades separadas. O ChatClient interpreta a intenção do usuário, o Tool Calling escolhe a ação e o serviço financeiro continua responsável por validar e salvar os dados.

Também pratiquei transcrição de áudio, geração de fala e testes de fluxos com dependências externas mockadas.

## Autor

Pedro Teles de Brito

GitHub: [PedroseleT](https://github.com/PedroseleT)

LinkedIn: [pedro-teless](http://www.linkedin.com/in/pedro-teless)
