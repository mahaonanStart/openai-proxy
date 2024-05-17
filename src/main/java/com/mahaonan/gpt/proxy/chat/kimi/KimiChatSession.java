package com.mahaonan.gpt.proxy.chat.kimi;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.mahaonan.gpt.proxy.chat.BaseChatSession;
import com.mahaonan.gpt.proxy.chat.ChatBot;
import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.chat.kimi.handler.KimiMessageHandler;
import com.mahaonan.gpt.proxy.config.properties.GptProxyProperties;
import com.mahaonan.gpt.proxy.helper.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * @author mahaonan
 */
@Component("kimiStream")
@Slf4j
public class KimiChatSession extends BaseChatSession {

    public static String URL = "https://kimi.moonshot.cn/api/chat/{chatId}/completion/stream";
    public static String currentChatId;

    public KimiChatSession(WebClient webClient, GptProxyProperties gptProxyProperties) {
        super(webClient, gptProxyProperties);
    }

    @Override
    protected ChatBot setChatBot() {
        return ChatBot.KIMI_STREAM_AI;
    }

    private Flux<String> postChatWithToken(KimiRequestModel requestModel, String token, int retryCount) {
        if (retryCount <= 0) {
            // 如果重试次数已经用完，就不再重试，而是直接抛出异常
            return Flux.error(new RuntimeException("Retry limit exceeded"));
        }
        return getWebClient().post()
                .uri(URL)
                .headers(httpHeaders -> {
                    buildHeaders(token).forEach(httpHeaders::add);
                })
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(JsonUtils.objectToJson(requestModel))
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> Mono.error(new KimiException(clientResponse.statusCode().value(), clientResponse.statusCode().getReasonPhrase())))
                .bodyToFlux(String.class).map(data -> {
                    KimiResponseModel responseModel = JsonUtils.parse(data, KimiResponseModel.class);
                    String event = responseModel.getEvent();
                    switch (event) {
                        case "ping":
                            return "";
                        case "search_plus":
                            KimiResponseModel.Msg msg = responseModel.getMsg();
                            String msgType = msg.getType();
                            switch (msgType) {
                                case "start":
                                    return "正在尝试为您在互联网搜索相关资料...\n";
                                case "start_res":
                                    return "开始获取资料...\n";
                                case "get_res":
                                    return String.format("获取到第%d条资料: [%s](%s)", msg.getSuccessNum(), msg.getTitle(), msg.getUrl()) + "\n";
                                case "done":
                                    return "找到了 " + msg.getUrlList().size() + "篇资料作为参考：" + "\n";
                                default:
                                    return "";
                            }
                        case "cmpl":
                            return responseModel.getText();
                        case "all_done":
                            return "[DONE]";
                    }
                    return "";
                })
                .onErrorResume(e -> {
                    if (e instanceof KimiException) {
                        KimiException exception = (KimiException) e;
                        if (exception.getCode() == 401) {
                            // 当发生401错误时，重新获取token并重新post
                            String newToken = KimiUtils.refreshToken();
                            log.info("refresh token, new token: {}", newToken);
                            return postChatWithToken(requestModel, newToken, retryCount - 1);
                        }
                    }
                    return Flux.error(e);
                });
    }


    @Override
    protected Flux<String> postChat(String question, List<ChatMessage> messages) {
        KimiRequestModel requestModel = new KimiRequestModel(question, Collections.singletonList(messages.get(messages.size() - 1)), new ArrayList<>(), false);
        Map<String, KimiMessageHandler> handlers = SpringUtil.getBeansOfType(KimiMessageHandler.class);
        for (KimiMessageHandler handler : handlers.values()) {
            if (handler.isMatch(requestModel)) {
                handler.handle(requestModel);
                if (StrUtil.isNotEmpty(handler.directReturn())) {
                    return Flux.just(handler.directReturn());
                }
            }
        }
        if (URL.contains("{chatId}")) {
            throw new RuntimeException("请先新建会话");
        }
        String token = KimiUtils.getToken();
        return postChatWithToken(requestModel, token, 2);
    }


    public Map<String, String> buildHeaders(String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        headers.put("Origin", "https://kimi.moonshot.cn");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36");
        return headers;
    }

    @Override
    protected boolean isEnd(StringBuilder totalMsg, String currMsg) {
        return "[DONE]".equals(currMsg);
    }
}
