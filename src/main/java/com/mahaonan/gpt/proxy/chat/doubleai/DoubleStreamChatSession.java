package com.mahaonan.gpt.proxy.chat.doubleai;

import com.mahaonan.gpt.proxy.chat.ChatBot;
import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.config.properties.GptProxyProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author mahaonan
 */
@Component("doubleStream")
@Slf4j
public class DoubleStreamChatSession extends DoubleChatSession {
    public DoubleStreamChatSession(WebClient webClient, GptProxyProperties properties) {
        super(webClient, properties);
    }

    @Override
    protected ChatBot setChatBot() {
        return ChatBot.DOUBLE_STREAM_AI;
    }

    @Override
    protected Flux<String> postChat(String question, List<ChatMessage> messages) {
        Flux<String> orginFlux = getWebClient().post()
                .uri(DOUBLE_API_URL)
                .headers(httpHeaders -> {
                    buildHeaders().forEach(httpHeaders::add);
                })
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(buildBody(messages, true)))
                .retrieve()
                .bodyToFlux(String.class);
        return orginFlux.map(data -> data + "\n");
    }

}
