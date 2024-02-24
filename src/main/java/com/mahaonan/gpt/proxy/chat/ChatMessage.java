package com.mahaonan.gpt.proxy.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author mahaonan
 * 该类不可被修改,否则会影响到历史会话
 */
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = -5879380541589379094L;

    private ChatMessage() {
    }

    @Getter
    @Setter
    private ChatRoleEnum role;
    @Getter
    private String content;
    @Getter
    @JsonIgnore
    private Boolean allowDeleted;

    public static ChatMessage build(ChatRoleEnum role, String content) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.role = role;
        chatMessage.content = content;
        chatMessage.allowDeleted = true;
        return chatMessage;
    }

    public static ChatMessage buildSystemMsg(String content) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.role = ChatRoleEnum.SYSTEM;
        chatMessage.content = content;
        chatMessage.allowDeleted = false;
        return chatMessage;
    }
}
