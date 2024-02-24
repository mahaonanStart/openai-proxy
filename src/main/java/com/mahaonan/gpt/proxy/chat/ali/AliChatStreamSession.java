package com.mahaonan.gpt.proxy.chat.ali;

import com.mahaonan.gpt.proxy.chat.ChatBot;
import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.config.properties.GptProxyProperties;
import com.mahaonan.gpt.proxy.helper.JsonUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * @author mahaonan
 */
@Component("aliStream")
@ConditionalOnProperty(name = "gpt.proxy.ali.enabled", havingValue = "true")
public class AliChatStreamSession extends AliChatSession {


    public AliChatStreamSession(WebClient webClient, GptProxyProperties gptProxyProperties) {
        super(webClient, gptProxyProperties);
    }

    @Override
    protected ChatBot setChatBot() {
        return ChatBot.ALI_STEAM_AI;
    }

    @Override
    protected Flux<String> postChat(String question, List<ChatMessage> messages) {
        StringBuilder prevMsg = new StringBuilder();
        Map<String, String> header = buildHeader(true);
        String body = buildBody(messages);
        Flux<String> originResult = getWebClient()
                .post()
                .uri(aliProperties.getUrl())
                .headers(httpHeaders -> {
                    header.forEach(httpHeaders::add);
                })
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class);
        return originResult.map(data -> {
            StringBuilder sb = new StringBuilder();
            String[] dataSplit = data.split("\n");
            for (String s : dataSplit) {
                AliChatResponse chatResponse = JsonUtils.parse(s, AliChatResponse.class);
                if (chatResponse == null) {
                    continue;
                }
                AliChatResponse.OutputData output = chatResponse.getOutput();
                String text = output.getText();
                if (prevMsg.length() > 0) {
                    //去掉之前的消息
                    text = text.substring(prevMsg.length());
                }
                prevMsg.append(text);
                sb.append(text);
            }
            return sb.toString();
        });
    }
}
