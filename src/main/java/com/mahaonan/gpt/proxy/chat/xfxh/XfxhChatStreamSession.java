package com.mahaonan.gpt.proxy.chat.xfxh;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.mahaonan.gpt.proxy.chat.ChatBot;
import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.config.properties.GptProxyProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.UnicastProcessor;

import java.net.URI;
import java.util.List;

/**
 * @author mahaonan
 */
@Component("xfxhStream")
@ConditionalOnProperty(name = "gpt.proxy.xfxh.enabled", havingValue = "true")
public class XfxhChatStreamSession extends XfxhChatSession {


    public XfxhChatStreamSession(WebClient webClient, GptProxyProperties gptProxyProperties) {
        super(webClient, gptProxyProperties);
    }

    @Override
    protected ChatBot setChatBot() {
        return ChatBot.XFXH_STREAM_AI;
    }

    @Override
    protected Flux<String> postChat(String question, List<ChatMessage> messages) {
        String url = getAuthUrl(xfxhProperties.getUrl(), xfxhProperties.getApiKey(), xfxhProperties.getApiSecret());
        WebSocketClient webSocketClient = SpringUtil.getBean(WebSocketClient.class);
        String requestJson = buildRequest(messages);
        UnicastProcessor<String> processor = UnicastProcessor.create();
        webSocketClient.execute(URI.create(url), session ->
                session.send(Mono.just(session.textMessage(requestJson)))
                        .thenMany(session
                                .receive()
                                .mapNotNull(msg -> dealOriginMsg(msg.getPayloadAsText())))
                        .doOnNext(text -> {
                            if (text.contains("[DONE]")) {
                                processor.onNext(text.replace("[DONE]", ""));
                                processor.onNext("[DONE]");
                            } else {
                                processor.onNext(text);
                            }
                        })
                        .doFinally(signalType -> {
                            // 在完成时关闭 WebSocket 连接
                            if (signalType.equals(SignalType.ON_COMPLETE) || signalType.equals(SignalType.ON_ERROR)) {
                                session.close();
                                processor.onComplete();
                            }
                        }).then()).subscribe();
        return processor;
    }

    @Override
    protected boolean isEnd(StringBuilder totalMsg, String currMsg) {
        return "[DONE]".equals(currMsg) && StrUtil.isNotEmpty(totalMsg);
    }
}
