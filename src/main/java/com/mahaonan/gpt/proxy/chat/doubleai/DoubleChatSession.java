package com.mahaonan.gpt.proxy.chat.doubleai;

import com.mahaonan.gpt.proxy.chat.BaseChatSession;
import com.mahaonan.gpt.proxy.chat.ChatBot;
import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.config.properties.DoubleProperties;
import com.mahaonan.gpt.proxy.config.properties.GptProxyProperties;
import com.mahaonan.gpt.proxy.helper.HttpClientPro;
import com.mahaonan.gpt.proxy.helper.JsonUtils;
import lombok.extern.slf4j.Slf4j;
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
@Component("double")
@Slf4j
@ConditionalOnProperty(name = "gpt.proxy.double-ai.enabled", havingValue = "true")
public class DoubleChatSession extends BaseChatSession {

    protected String DOUBLE_API_URL;
    protected String API_KEY;
    protected DoubleProperties doubleProperties;
    public DoubleChatSession(WebClient webClient, GptProxyProperties properties) {
        super(webClient, properties);
        this.doubleProperties = getGptProxyProperties().getDoubleAi();
        this.DOUBLE_API_URL = doubleProperties.getBaseUrl() + "/api/v1/chat";
        this.API_KEY = doubleProperties.getApiKey();
    }

    @Override
    protected ChatBot setChatBot() {
        return ChatBot.DOUBLE_AI;
    }

    @Override
    protected Flux<String> postChat(String question, List<ChatMessage> messages) {
        String body = buildBody(messages, false);
        Map<String, String> headers = buildHeaders();
        String result = HttpClientPro.getInstance().postJson(DOUBLE_API_URL, body, headers, null, null, String.class);
        return Flux.just(result);
    }

    protected Map<String, String> buildHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer " + API_KEY);
        headers.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Code/1.87.1 Chrome/118.0.5993.159 Electron/27.3.2 Safari/537.36");
        return headers;
    }


    protected String buildBody(List<ChatMessage> messages, boolean stream) {
        return JsonUtils.objectToJson(DoubleRequestModel.build(API_KEY, messages , doubleProperties.getModel()));
    }
}
