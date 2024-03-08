package com.mahaonan.gpt.proxy.chat.doubleai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.chat.ChatRoleEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class DoubleRequestModel implements Serializable {
    private static final long serialVersionUID = -8712535888045728665L;

    @JsonProperty("api_key")
    private String apiKey;

    private List<DoubleMessage> messages;

    @JsonProperty("chat_model")
    private String chatModel;


    @Data
    public static class DoubleMessage {
        private ChatRoleEnum role;
        private String message;
        private List<String> codeContexts;

        public static List<DoubleMessage> build(List<ChatMessage> chatMessages) {
            return chatMessages.stream().map(chatMessage -> {
                DoubleMessage doubleMessage = new DoubleMessage();
                doubleMessage.role = chatMessage.getRole();
                doubleMessage.message = chatMessage.getContent();
                if (chatMessage.getRole() == ChatRoleEnum.USER) {
                    doubleMessage.codeContexts = new ArrayList<>();
                }
                return doubleMessage;
            }).collect(Collectors.toList());
        }
    }

    public static DoubleRequestModel build(String apiKey, List<ChatMessage> messages, String chatModel) {
        DoubleRequestModel doubleRequestModel = new DoubleRequestModel();
        doubleRequestModel.apiKey = apiKey;
        doubleRequestModel.chatModel = chatModel;
        doubleRequestModel.messages = DoubleMessage.build(messages);
        return doubleRequestModel;
    }
}
