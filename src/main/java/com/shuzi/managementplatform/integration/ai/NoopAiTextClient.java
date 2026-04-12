package com.shuzi.managementplatform.integration.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(AiTextClient.class)
public class NoopAiTextClient implements AiTextClient {

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        throw new IllegalStateException("AI client disabled");
    }
}
