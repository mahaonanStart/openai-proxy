package com.mahaonan.gpt.proxy.chat;

import com.mahaonan.gpt.proxy.chat.model.ChatGptAnswer;
import com.mahaonan.gpt.proxy.config.properties.GptProxyProperties;
import com.mahaonan.gpt.proxy.helper.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author mahaonan
 */
@Slf4j
public abstract class BaseChatSession {

    @Getter
    private final WebClient webClient;

    @Getter
    private GptProxyProperties gptProxyProperties;

    @Getter
    private ChatBot chatBot;

    protected abstract ChatBot setChatBot();

    public BaseChatSession(WebClient webClient, GptProxyProperties gptProxyProperties) {
        this.chatBot = setChatBot();
        this.webClient = webClient;
        this.gptProxyProperties = gptProxyProperties;
    }

    /**
     * 发送请求实现的方法
     * @param question 本次询问的问题
     * @param messages 消息上下文,包含本次询问的问题和历史对话记录
     * @return 返回的消息流,只包含文本消息和结束标志,会自动转为gpt标准格式
     */
    protected abstract Flux<String> postChat(String question, List<ChatMessage> messages);


    /**
     * 会话是否结束的标记
     * @param totalMsg 当前已经收到的全部回答
     * @param currMsg 流式传输当前回答
     * @return
     */
    protected boolean isEnd(StringBuilder totalMsg, String currMsg) {
        return false;
    }

    /**
     * 权重
     */
    public int weight() {
        return 1;
    }


    public Flux<String> chat(List<ChatMessage> messages) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        if (!ChatBot.isOpenAi(this.getChatBot())) {
            //非openai请求需要处理system消息,转为user请求
            messages.forEach(message -> {
                if (ChatRoleEnum.SYSTEM.equals(message.getRole())) {
                    chatMessages.add(ChatMessage.build(ChatRoleEnum.USER, message.getContent()));
                    chatMessages.add(ChatMessage.build(ChatRoleEnum.ASSISTANT, "好的"));
                }else {
                    chatMessages.add(message);
                }
            });
        }else {
            chatMessages.addAll(messages);
        }
        Flux<String> webFlux = postChat(messages.get(messages.size() - 1).getContent(), chatMessages);
        if (ChatBot.isOpenAi(this.getChatBot())) {
            return webFlux;
        }
        //转换为gpt标准格式
        String id = UUID.randomUUID().toString();
        StringBuilder sb = new StringBuilder();
        Flux<String> result;
        if (this.getChatBot().isStream()) {
            result = webFlux.map(data -> {
                if (this.isEnd(sb, data)) {
                    return "[DONE]";
                }
                sb.append(data);
                ChatGptAnswer answer = new ChatGptAnswer();
                answer.setId(id);
                answer.setObject("chat.completion.chunk");
                answer.setCreated(System.currentTimeMillis());
                answer.setModel("gpt-35-turbo");
                ChatGptAnswer.Choices choices = new ChatGptAnswer.Choices();
                choices.setIndex(0);
                ChatGptAnswer.Message message = new ChatGptAnswer.Message();
                message.setRole("assistant");
                message.setContent(data);
                choices.setDelta(message);
                answer.setChoices(Collections.singletonList(choices));
                return JsonUtils.objectToJson(answer);
            });
        }else {
            result = webFlux.map(data -> {
                ChatGptAnswer answer = new ChatGptAnswer();
                ChatGptAnswer.Choices choices = new ChatGptAnswer.Choices();
                ChatGptAnswer.Message message = new ChatGptAnswer.Message();
                answer.setId(id);
                answer.setObject("chat.completion.chunk");
                answer.setCreated(System.currentTimeMillis());
                answer.setModel("gpt-35-turbo");
                message.setRole("assistant");
                message.setContent(data);
                choices.setMessage(message);
                choices.setFinish_reason("stop");
                answer.setChoices(Collections.singletonList(choices));
                sb.append(data);
                return JsonUtils.objectToJson(answer);
            });
        }
        return result.doOnComplete(() -> {
            if (gptProxyProperties.isPrintLog()) {
                log.info("AI回答: {}", sb);
            }
        }).doOnError(e -> {
            log.error("AI回答异常", e);
        });

    }



}
