package com.mahaonan.gpt.proxy.chat;

import lombok.Data;

/**
 * @author mahaonan
 */
@Data
public class ChatResponse {

    private String text;

    private ResponseStatus status;

    public enum ResponseStatus {
        SUCCESS,
        ERROR
    }

    public static ChatResponse error() {
        ChatResponse response = new ChatResponse();
        response.setStatus(ResponseStatus.ERROR);
        return response;
    }

}
