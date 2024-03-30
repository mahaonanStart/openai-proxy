package com.mahaonan.gpt.proxy.chat.gpt;

import com.mahaonan.gpt.proxy.chat.BaseChatSession;
import com.mahaonan.gpt.proxy.chat.ChatBot;
import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.config.properties.GptProxyProperties;
import com.mahaonan.gpt.proxy.config.properties.OpenaiProperties;
import com.mahaonan.gpt.proxy.helper.HttpClientPro;
import com.mahaonan.gpt.proxy.helper.HttpRequestHolder;
import com.mahaonan.gpt.proxy.helper.JsonUtils;
import com.mahaonan.gpt.proxy.model.GptProxyRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mahaonan
 */
@Component("openai")
@ConditionalOnProperty(name = "gpt.proxy.openai.enabled", havingValue = "true")
public class OpenaiChatSession extends BaseChatSession {

    protected String url;
    protected OpenaiProperties openaiProperties;

    public OpenaiChatSession(WebClient webClient, GptProxyProperties gptProxyProperties) {
        super(webClient, gptProxyProperties);
        this.openaiProperties = getGptProxyProperties().getOpenai();
        this.url = openaiProperties.getBaseUrl() + "/v1/chat/completions";
    }

    @Override
    protected ChatBot setChatBot() {
        return ChatBot.OPEN_AI;
    }

    @Override
    protected Flux<String> postChat(String question, List<ChatMessage> messages) {
        Map<String, Object> params = buildParams(messages, false);
        return Flux.just(HttpClientPro.getInstance().postJson(url, JsonUtils.objectToJson(params), getHeader(), null, null, String.class));
    }

    protected Map<String, Object> buildParams(List<ChatMessage> messages, boolean stream) {
        GptProxyRequest request = HttpRequestHolder.get();
        Map<String, Object> params = new HashMap<>();
        params.put("messages", messages);
        params.put("model", request.getModel());
        params.put("temperature", request.getTemperature());
        params.put("top_p", request.getTopP());
        params.put("presence_penalty", request.getPresencePenalty());
        params.put("frequency_penalty", request.getFrequencyPenalty());
        params.put("stream", stream);
        params.put("tools", request.getTools());
        return params;
    }

    protected Map<String, String> getHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + openaiProperties.getApiKey());
        return headers;
    }

}
