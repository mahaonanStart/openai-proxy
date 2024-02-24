package com.mahaonan.gpt.proxy.chat.xfxh;

import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * @author mahaonan
 */
@Data
public class XfxhChatMessage {


    private Header header;
    private ChatParameter parameter;
    private Payload payload;

    @Data
    public static class Header {
        private String app_id;
        private String uid;

        public Header(String app_id) {
            this.app_id = app_id;
            this.uid = UUID.randomUUID().toString().substring(0, 10);
        }
    }

    @Data
    public static class ChatParameter {
        private Chat chat;
    }

    @Data
    public static class Chat {
        private String domain;
        private double temperature;
        private int max_tokens;
    }

    @Data
    public static class MessageContent {
        private String role;
        private String content;
    }

    @Data
    public static class Message {
        private List<MessageContent> text;
    }


    @Data
    public static class Payload {
        private Message message;
    }

}
