package com.mahaonan.gpt.proxy.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.mahaonan.gpt.proxy.chat.BaseChatSession;
import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.config.ApplicationConfig.PollingChatSessionHolder;
import com.mahaonan.gpt.proxy.config.properties.GptProxyProperties;
import com.mahaonan.gpt.proxy.model.GptProxyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author: M˚Haonan
 * @date: 2023/5/15 21:35
 * @description:
 */
@Service
@Slf4j
public class ChatGptProxyService {

    @Resource
    private GptProxyProperties gptProxyProperties;

    public Flux<String> chat(@RequestBody GptProxyRequest request) {
        List<ChatMessage> messages = request.getMessages();
        if (CollectionUtil.isEmpty(messages)) {
           return Flux.just("");
        }
        String question = messages.get(messages.size() - 1).getContent();
        //判断是否以特殊指令开头
        if (StrUtil.isEmpty(question)) {
            return Flux.just("");
        }
        String botName = request.getBotType().getName();
        if (question.startsWith("/")) {
            botName = question.substring(1, question.indexOf(" "));
            question = question.substring(question.indexOf(" ") + 1);
        }
        if (gptProxyProperties.isPrintLog()) {
            log.info("request: {}", question);
        }
        return getChatSession(request, botName).chat(request.getMessages());
    }

    private BaseChatSession getChatSession(GptProxyRequest request, String botName) {
        if ("all".equals(botName)) {
            //轮询获取
            botName = PollingChatSessionHolder.getBotName();
        }
        Map<String, BaseChatSession> chatSessionMap = SpringUtil.getBeansOfType(BaseChatSession.class);
        botName = botName + (Objects.equals(true, request.getStream()) ? "Stream" : "");
        return chatSessionMap.get(botName);
    }

}
