package com.mahaonan.gpt.proxy.chat.bito;

import com.mahaonan.gpt.proxy.chat.BaseChatSession;
import com.mahaonan.gpt.proxy.chat.ChatBot;
import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.chat.ChatRoleEnum;
import com.mahaonan.gpt.proxy.config.properties.BitoProperties;
import com.mahaonan.gpt.proxy.config.properties.GptProxyProperties;
import com.mahaonan.gpt.proxy.helper.HttpClientPro;
import com.mahaonan.gpt.proxy.helper.HttpRequestHolder;
import com.mahaonan.gpt.proxy.helper.JsonUtils;
import com.mahaonan.gpt.proxy.model.GptProxyRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mahaonan
 */
@Component("bito")
@ConditionalOnProperty(name = "gpt.proxy.bito.enabled", havingValue = "true")
public class BitoChatSession extends BaseChatSession {

    public static final Map<String, String> commonHeaders = new HashMap<>();
    public final Pattern BITO_DATA_PATTERN = Pattern.compile("data:\\s*\\{.*}");

    protected BitoProperties bitoProperties;

    static {
        commonHeaders.put("x-clientinfo", "MacIntel Gecko 20030107#IntelliJ IDEA 2022.3.1#1.0.134#f23f5e28-2e0e-4099-807c-e109d73ffbdf");
        commonHeaders.put("accept", "*/*");
        commonHeaders.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.102 Safari/537.36");
        commonHeaders.put("origin", "http://ideapp");
        commonHeaders.put("referer", "http://ideapp/");
        commonHeaders.put("sec-ch-ua-platform", "Mac OS X");
        commonHeaders.put("sec-ch-ua", "\"Chromium\";v=\"104\"");
        commonHeaders.put("sec-fetch-site", "cross-site");
        commonHeaders.put("sec-fetch-mode", "cors");
        commonHeaders.put("sec-fetch-dest", "empty");
        commonHeaders.put("accept-encoding", "gzip, deflate, br");
    }

    public BitoChatSession(WebClient webClient, GptProxyProperties gptProxyProperties) {
        super(webClient, gptProxyProperties);
        this.bitoProperties = getGptProxyProperties().getBito();
    }


    @Override
    protected ChatBot setChatBot() {
        return ChatBot.BITO_AI;
    }


    @Override
    protected Flux<String> postChat(String question, List<ChatMessage> messages) {
        BitoRequest bitoRequest = buildRequest(question, messages);
        bitoRequest.setStream(false);
        Map<String, String> headers = new HashMap<>(commonHeaders);
        headers.put("authorization", bitoRequest.getAuthToken());
        String res = HttpClientPro.getInstance().postJson(bitoProperties.getUrl(), JsonUtils.objectToJson(bitoRequest), headers, null,null, String.class);
        Matcher matcher = BITO_DATA_PATTERN.matcher(res);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String s = matcher.group();
            s = s.substring(5);
            sb.append(JsonUtils.strExpression(s, "choices[0].text"));
        }
        return Flux.just(sb.toString());
    }

    protected BitoRequest buildRequest(String question, List<ChatMessage> messages) {
        GptProxyRequest proxyRequest = HttpRequestHolder.get();
        BitoRequest request = BitoRequest.builder()
                .bitoUserId(bitoProperties.getBitoUserId())
                .email(bitoProperties.getEmail())
                .ideName("JB")
                .prompt(question)
                .uid("f23f5e28-2e0e-4099-807c-e109d73ffbdf")
                .wsId(bitoProperties.getWsId())
                .stream(false)
                .requestId("43b96d8b-cf50-648a-5c4f-e7f6ea3401f9")
                .sessionId("07c4a456-4171-5f3f-c8da-9c2f5e283dde")
                .context(new ArrayList<>())
                .authToken(bitoProperties.getAuthToken())
                .outputLanguage("Chinese (Simplified)")
                .aiModelType(proxyRequest.getModel().startsWith("gpt-4") ? "ADVANCED" : "BASIC")
                .agentAPI("https://lca.bito.ai/ai/v2/selectAgent/?processSilently=true")
                .embeddingAPI("https://lca.bito.ai/ai/v2/embedding/?processSilently=true")
                .contextAnswerAPI("https://lca.bito.ai/ai/v2/contextAnswer/?processSilently=true")
                .method("POST")
                .topN(String.valueOf(proxyRequest.getTopP()))
                .topNThreshold("0.7")
                .build();
        for (ChatMessage message : messages) {
            BitoRequest.Context context = new BitoRequest.Context();
            if (message.getRole() == ChatRoleEnum.USER) {
                context.setQuestion(message.getContent());
            } else {
                context.setAnswer(message.getContent());
            }
            request.getContext().add(context);
        }
        return request;
    }
}
