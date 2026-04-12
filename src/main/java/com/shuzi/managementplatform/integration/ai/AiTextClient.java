package com.shuzi.managementplatform.integration.ai;

public interface AiTextClient {
    String complete(String systemPrompt, String userPrompt);
}
