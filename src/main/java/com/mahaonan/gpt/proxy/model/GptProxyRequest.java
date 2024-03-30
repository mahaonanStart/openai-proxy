package com.mahaonan.gpt.proxy.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private Boolean stream;
    private BotType botType;
    private Double temperature;
    @JsonProperty("top_p")
    private Double topP;
    private String model;
    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;
    @JsonProperty("presence_penalty")
    private Double presencePenalty;
    private List<Tool> tools;
}
