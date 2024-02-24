package com.mahaonan.gpt.proxy.chat.gemini;

import com.mahaonan.gpt.proxy.chat.BaseChatSession;
import com.mahaonan.gpt.proxy.chat.ChatBot;
import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.config.properties.GeminiProperties;
import com.mahaonan.gpt.proxy.config.properties.GptProxyProperties;
import com.mahaonan.gpt.proxy.helper.HttpClientPro;
import com.mahaonan.gpt.proxy.helper.JsonUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author mahaonan
 */
@Component("gemini")
@ConditionalOnProperty(name = "gpt.proxy.gemini.enabled", havingValue = "true")
public class GeminiChatSession extends BaseChatSession {

    protected GeminiProperties geminiProperties;
    private final String geminiUrl;

    public GeminiChatSession(WebClient webClient, GptProxyProperties gptProxyProperties) {
        super(webClient, gptProxyProperties);
        this.geminiProperties = getGptProxyProperties().getGemini();
        this.geminiUrl = geminiProperties.getBaseUrl() + "/v1beta/models/gemini-pro:generateContent?key=" + geminiProperties.getKey();
    }

    @Override
    protected ChatBot setChatBot() {
        return ChatBot.GEMINI_AI;
    }

    @Override
    protected Flux<String> postChat(String question, List<ChatMessage> messages) {
        ContentData contents = ContentData.build(messages);
        ResponseData responseData = HttpClientPro.getInstance().postJson(geminiUrl, JsonUtils.objectToJson(contents), null, null, null, ResponseData.class);
        if (responseData == null) {
            return Flux.just("");
        }
        List<ResponseData.Part> parts = responseData.getCandidates().get(0).getContent().getParts();
        return Flux.just(parts.get(parts.size() - 1).getText());
    }
}
