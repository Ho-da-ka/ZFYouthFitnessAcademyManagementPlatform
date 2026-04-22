package com.shuzi.managementplatform.integration.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.ai", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoopAiTextClient implements AiTextClient {

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        throw new IllegalStateException("AI client disabled");
    }
}
