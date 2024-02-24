package com.mahaonan.gpt.proxy.chat.bito;

import com.mahaonan.gpt.proxy.chat.ChatBot;
import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.config.properties.GptProxyProperties;
import com.mahaonan.gpt.proxy.helper.JsonUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @author mahaonan
 */
@Component("bitoStream")
@ConditionalOnProperty(name = "gpt.proxy.bito.enabled", havingValue = "true")
public class BitoStreamChatSession extends BitoChatSession {


    public BitoStreamChatSession(WebClient webClient, GptProxyProperties gptProxyProperties) {
        super(webClient, gptProxyProperties);
    }

    @Override
    protected Flux<String> postChat(String question, List<ChatMessage> messages) {
        BitoRequest bitoRequest = buildRequest(question, messages);
        bitoRequest.setStream(true);
        Map<String, String> headers = new HashMap<>(commonHeaders);
        headers.put("authorization", bitoRequest.getHeaderAuthorization());
        Flux<String> originResult = getWebClient().post()
                .uri(bitoProperties.getUrl())
                .headers(httpHeaders -> {
                    headers.forEach(httpHeaders::add);
                })
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(bitoRequest))
                .retrieve()
                .bodyToFlux(String.class);
        return originResult.map(data -> {
            if ("data[DONE]".equals(data)) {
                return data;
            }
            Matcher matcher = BITO_DATA_PATTERN.matcher(data);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                String s = matcher.group();
                s = s.substring(5);
                sb.append(JsonUtils.strExpression(s, "choices[0].text"));
            }
            return sb.toString();
        });
    }

    @Override
    protected ChatBot setChatBot() {
        return ChatBot.BITO_STEAM_AI;
    }
}
