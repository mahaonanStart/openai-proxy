package com.mahaonan.gpt.proxy.chat.gpt;

import com.mahaonan.gpt.proxy.chat.ChatBot;
import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.config.properties.GptProxyProperties;
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
@Component("openaiStream")
@ConditionalOnProperty(name = "gpt.proxy.openai.enabled", havingValue = "true")
public class OpenaiStreamChatSession extends OpenaiChatSession {

    public OpenaiStreamChatSession(WebClient webClient, GptProxyProperties gptProxyProperties) {
        super(webClient, gptProxyProperties);
    }

    @Override
    protected ChatBot setChatBot() {
        return ChatBot.OPEN_STREAM_AI;
    }

    @Override
    protected Flux<String> postChat(String question, List<ChatMessage> messages) {
        return getWebClient().post()
                .uri(url)
                .headers(httpHeaders -> {
                    getHeader().forEach(httpHeaders::add);
                })
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(buildParams(messages, true)))
                .retrieve()
                .bodyToFlux(String.class);
    }

}
