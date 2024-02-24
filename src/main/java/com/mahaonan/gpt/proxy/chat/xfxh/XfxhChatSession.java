package com.mahaonan.gpt.proxy.chat.xfxh;

import com.mahaonan.gpt.proxy.chat.BaseChatSession;
import com.mahaonan.gpt.proxy.chat.ChatBot;
import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.config.properties.GptProxyProperties;
import com.mahaonan.gpt.proxy.config.properties.XfxhProperties;
import com.mahaonan.gpt.proxy.helper.HttpClientUtils;
import com.mahaonan.gpt.proxy.helper.JsonUtils;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;

/**
 * @author mahaonan
 */
@Component("xfxh")
@Slf4j
@ConditionalOnProperty(name = "gpt.proxy.xfxh.enabled", havingValue = "true")
public class XfxhChatSession extends BaseChatSession {

    protected XfxhProperties xfxhProperties;


    public XfxhChatSession(WebClient webClient, GptProxyProperties gptProxyProperties) {
        super(webClient, gptProxyProperties);
        this.xfxhProperties = getGptProxyProperties().getXfxh();
    }

    @Override
    protected ChatBot setChatBot() {
        return ChatBot.XFXH_AI;
    }

    @Override
    protected Flux<String> postChat(String question, List<ChatMessage> messages) {
        String url = getAuthUrl(xfxhProperties.getUrl(), xfxhProperties.getApiKey(), xfxhProperties.getApiSecret());
        String requestJson = buildRequest(messages);
        String result = wssRequest(url, requestJson);
        return Flux.just(result);
    }

    @SneakyThrows
    public String wssRequest(String url, String body) {
        StringBuilder sb = new StringBuilder();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        // 创建WebSocket连接
        WebSocket webSocket = HttpClient.newHttpClient().newWebSocketBuilder()
                .buildAsync(URI.create(url), new WebSocket.Listener() {
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        WebSocket.Listener.super.onOpen(webSocket);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        XfxhChatSession.JsonParse myJsonParse = JsonUtils.parse(data.toString(), XfxhChatSession.JsonParse.class);
                        if (myJsonParse.header.code != 0) {
                            countDownLatch.countDown();
                            return null;
                        }
                        List<XfxhChatSession.Text> textList = myJsonParse.payload.choices.text;
                        for (XfxhChatSession.Text temp : textList) {
                            String content = temp.content;
                            content = content.replace("[code]", "```").replace("[/code]", "```");
                            sb.append(content);
                        }
                        if (myJsonParse.header.status == 2) {
                            // 可以关闭连接，释放资源
                            countDownLatch.countDown();
                            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "ok");
                        }
                        return WebSocket.Listener.super.onText(webSocket, data, last);
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        WebSocket.Listener.super.onError(webSocket, error);
                        countDownLatch.countDown();
                    }
                }).join(); // 等待连接完成
        // 发送消息
        webSocket.sendText(body, true);
        countDownLatch.await();
        return sb.toString();
    }

    protected String dealOriginMsg(String originText) {
        JsonParse myJsonParse = JsonUtils.parse(originText, JsonParse.class);
        if (myJsonParse.header.code != 0) {
            return null;
        }
        List<Text> textList = myJsonParse.payload.choices.text;
        StringBuilder sb = new StringBuilder();
        for (Text temp : textList) {
            String content = temp.content;
            content = content.replace("[code]", "```").replace("[/code]", "```");
            sb.append(content);
        }
        return sb.toString();
    }

    protected String buildRequest(List<ChatMessage> messages) {
        XfxhChatMessage xfxhChatMessage = new XfxhChatMessage();
        XfxhChatMessage.Header header = new XfxhChatMessage.Header(xfxhProperties.getAppId());
        xfxhChatMessage.setHeader(header);
        XfxhChatMessage.ChatParameter parameter = new XfxhChatMessage.ChatParameter();
        XfxhChatMessage.Chat chat = new XfxhChatMessage.Chat();
        chat.setDomain("generalv3");
        chat.setMax_tokens(2048);
        chat.setTemperature(0.5);
        parameter.setChat(chat);
        xfxhChatMessage.setParameter(parameter);
        XfxhChatMessage.Payload payload = getPayload(messages);
        xfxhChatMessage.setPayload(payload);
        return JsonUtils.objectToJson(xfxhChatMessage);
    }

    protected XfxhChatMessage.Payload getPayload(List<ChatMessage> messages) {
        XfxhChatMessage.Payload payload = new XfxhChatMessage.Payload();
        XfxhChatMessage.Message message = new XfxhChatMessage.Message();
        List<XfxhChatMessage.MessageContent> text = new ArrayList<>();
        for (ChatMessage temp : messages) {
            XfxhChatMessage.MessageContent messageContent = new XfxhChatMessage.MessageContent();
            messageContent.setRole(temp.getRole().getCode());
            messageContent.setContent(temp.getContent());
            text.add(messageContent);
        }
        message.setText(text);
        payload.setMessage(message);
        return payload;
    }

    @SneakyThrows
    public static String getAuthUrl(String hostUrl, String apiKey, String apiSecret) {
        URL url = new URL(hostUrl);
        // 时间
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        // 拼接
        String preStr = "host: " + url.getHost() + "\n" +
                "date: " + date + "\n" +
                "GET " + url.getPath() + " HTTP/1.1";
        // System.err.println(preStr);
        // SHA256加密
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        // Base64加密
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        // 拼接
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        // 拼接地址
        Map<String, String> params = new HashMap<>();
        params.put("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8)));
        params.put("date", date);
        params.put("host", url.getHost());
        String authUrl = HttpClientUtils.appendParams("https://" + url.getHost() + url.getPath(), params, "UTF-8");
        return authUrl.replace("http://", "ws://").replace("https://", "wss://");
    }

    @Data
    public static class JsonParse {
        Header header;
        Payload payload;
    }

    @Data
    public static class Header {
        int code;
        int status;
        String sid;
    }

    @Data
    public static class Payload {
        Choices choices;
    }

    @Data
    public static class Choices {
        List<Text> text;
    }

    @Data
    public static class Text {
        String role;
        String content;
    }

}
