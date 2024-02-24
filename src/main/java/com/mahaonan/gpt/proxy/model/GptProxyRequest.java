package com.mahaonan.gpt.proxy.model;

import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.constant.BotType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author: M˚Haonan
 * @date: 2023/5/15 21:33
 * @description:
 */
@Data
public class GptProxyRequest implements Serializable {

    private List<ChatMessage> messages;

    private String model;

    private Boolean stream;

    private String prompt;

    private BotType botType;
}
