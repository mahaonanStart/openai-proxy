package com.mahaonan.gpt.proxy.chat.copilot;

import cn.hutool.core.util.StrUtil;
import com.mahaonan.gpt.proxy.chat.ChatBot;
import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.config.properties.GptProxyProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author mahaonan
 */
@Component("copilotStream")
@ConditionalOnProperty(name = "gpt.proxy.copilot.enabled", havingValue = "true")
@Slf4j
public class CopilotStreamChatSession extends CopilotChatSession{

    public CopilotStreamChatSession(WebClient webClient, GptProxyProperties gptProxyProperties) {
        super(webClient, gptProxyProperties);
    }
    @Override
    protected ChatBot setChatBot() {
        return ChatBot.COPILOT_STREAM_AI;
    }

    @Override
    protected Flux<String> postChat(String question, List<ChatMessage> messages) {
        Flux<String> orginFlux = getWebClient().post()
                .uri(COPILOT_URL)
                .headers(httpHeaders -> {
                    buildHeader().forEach(httpHeaders::add);
                })
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(buildParams(messages, true)))
                .retrieve()
                .bodyToFlux(String.class).onErrorResume(e -> {
                    log.error("copilot stream error", e);
                    return Flux.just("[DONE]");
                });
        return orginFlux.filter(StrUtil::isNotBlank).map(data -> data.substring("data:".length()));
    }
}
