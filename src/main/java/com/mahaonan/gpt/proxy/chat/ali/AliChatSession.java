package com.mahaonan.gpt.proxy.chat.ali;

import cn.hutool.core.util.StrUtil;
import com.mahaonan.gpt.proxy.chat.BaseChatSession;
import com.mahaonan.gpt.proxy.chat.ChatBot;
import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.config.properties.AliProperties;
import com.mahaonan.gpt.proxy.config.properties.GptProxyProperties;
import com.mahaonan.gpt.proxy.helper.HttpClientPro;
import com.mahaonan.gpt.proxy.helper.JsonUtils;
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
@Component("ali")
@ConditionalOnProperty(name = "gpt.proxy.ali.enabled", havingValue = "true")
public class AliChatSession extends BaseChatSession {

    protected AliProperties aliProperties;

    public AliChatSession(WebClient webClient, GptProxyProperties gptProxyProperties) {
        super(webClient, gptProxyProperties);
        this.aliProperties = gptProxyProperties.getAli();
    }


    @Override
    protected ChatBot setChatBot() {
        return ChatBot.ALI_AI;
    }

    @Override
    protected Flux<String> postChat(String question, List<ChatMessage> messages) {
        AliChatResponse chatResponse = HttpClientPro.getInstance().postJson(aliProperties.getUrl(), buildBody(messages), buildHeader(false),
                "utf-8", "utf-8", AliChatResponse.class);
        if (chatResponse == null || StrUtil.isBlank(chatResponse.getOutput().getText())) {
            return Flux.just("");
        }
        return Flux.just(chatResponse.getOutput().getText());
    }

    protected Map<String, String> buildHeader(boolean stream) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + aliProperties.getApiKey());
        if (stream) {
            headers.put("X-DashScope-SSE", "enable");
        }
        return headers;
    }

    protected String buildBody(List<ChatMessage> messages) {
        Map<String, Object> params = new HashMap<>();
        params.put("model", aliProperties.getModel());
        params.put("input", AliChatRequestInput.build(messages));
        params.put("parameters", AliChatRequestParameter.buildParameter(true));
        return JsonUtils.objectToJson(params);
    }

}
