package com.shuzi.managementplatform.integration.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuzi.managementplatform.config.AiGenerationProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

class OpenAiCompatibleAiTextClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void completeShouldCallOpenAiCompatibleDeepSeekChatCompletions() throws Exception {
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> requestBody = new AtomicReference<>();

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/chat/completions", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

            byte[] response = """
                    {"choices":[{"message":{"content":"DeepSeek summary"}}]}
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        try {
            AiGenerationProperties properties = new AiGenerationProperties();
            properties.setBaseUrl("http://localhost:" + server.getAddress().getPort());
            properties.setApiKey("test-token");
            properties.setModel("deepseek-v4-flash");
            properties.setTimeout(Duration.ofSeconds(3));

            OpenAiCompatibleAiTextClient client = new OpenAiCompatibleAiTextClient(properties);

            String result = client.complete("system prompt", "user prompt");

            Assertions.assertEquals("DeepSeek summary", result);
            Assertions.assertEquals("Bearer test-token", authorization.get());

            JsonNode body = objectMapper.readTree(requestBody.get());
            Assertions.assertEquals("deepseek-v4-flash", body.get("model").asText());
            Assertions.assertFalse(body.get("stream").asBoolean());
            Assertions.assertEquals("system", body.get("messages").get(0).get("role").asText());
            Assertions.assertEquals("system prompt", body.get("messages").get(0).get("content").asText());
            Assertions.assertEquals("user", body.get("messages").get(1).get("role").asText());
            Assertions.assertEquals("user prompt", body.get("messages").get(1).get("content").asText());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void completeShouldRejectMissingApiKeyBeforeCallingProvider() {
        AiGenerationProperties properties = new AiGenerationProperties();
        properties.setBaseUrl("http://localhost:1");
        properties.setApiKey(" ");
        properties.setModel("deepseek-v4-flash");

        OpenAiCompatibleAiTextClient client = new OpenAiCompatibleAiTextClient(properties);

        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> client.complete("system prompt", "user prompt")
        );
        Assertions.assertEquals("AI API key is not configured", exception.getMessage());
    }
}
