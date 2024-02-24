package com.mahaonan.gpt.proxy.chat.gemini;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.mahaonan.gpt.proxy.chat.BaseChatSession;
import com.mahaonan.gpt.proxy.chat.ChatBot;
import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.config.properties.GeminiProperties;
import com.mahaonan.gpt.proxy.config.properties.GptProxyProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

/**
 * @author mahaonan
 */
@Component("geminiStream")
@ConditionalOnProperty(name = "gpt.proxy.gemini.enabled", havingValue = "true")
public class GeminiChatStreamSession extends BaseChatSession {

    private final String geminiStreamUrl;

    private final GeminiProperties geminiProperties;

    private final JsonFactory jsonFactory = new JsonFactory();


    public GeminiChatStreamSession(WebClient webClient, GptProxyProperties gptProxyProperties) {
        super(webClient, gptProxyProperties);
        this.geminiProperties = getGptProxyProperties().getGemini();
        this.geminiStreamUrl = geminiProperties.getBaseUrl() + "/v1beta/models/gemini-pro:streamGenerateContent?key=" + geminiProperties.getKey();
    }

    @Override
    protected ChatBot setChatBot() {
        return ChatBot.GEMINI_STREAM_AI;
    }

    @Override
    protected Flux<String> postChat(String question, List<ChatMessage> messages) {
        final StringBuilder sb = new StringBuilder();
        final StringBuilder prev = new StringBuilder();
        ContentData contents = ContentData.build(messages);
        return getWebClient().post()
                .uri(geminiStreamUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(contents))
                .retrieve()
                .bodyToFlux(String.class)
                .map(data -> {
                    sb.append(data);
                    String text = "";
                    try (JsonParser jsonParser = jsonFactory.createParser(sb.toString())) {
                        while (true) {
                            try {
                                if (jsonParser.nextToken() == null) {
                                    break;
                                }
                                if (jsonParser.getCurrentLocation().getCharOffset() <= prev.toString().length()) {
                                    jsonParser.nextToken();
                                    continue;
                                }
                                if (jsonParser.currentToken() == JsonToken.FIELD_NAME && "text".equals(jsonParser.getCurrentName())) {
                                    text = jsonParser.nextTextValue();
                                }
                            } catch (JsonParseException e) {
                                break;
                            }
                        }
                        prev.append(data);
                        return text;
                    } catch (IOException ignored) {
                        // Handle or log exception
                    }
                    return text;
                });
    }
}
