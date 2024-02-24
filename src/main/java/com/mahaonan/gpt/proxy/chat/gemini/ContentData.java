package com.mahaonan.gpt.proxy.chat.gemini;

import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.chat.ChatRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mahaonan
 */
@Data
public class ContentData {


    private List<Content> contents;


    @Data
    public static class Content {
        private String role;
        private List<Part> parts;
    }


    @Data
    @AllArgsConstructor
    public static class Part {
        private String text;
    }


    public static ContentData build(List<ChatMessage> messages) {
        ContentData contentData = new ContentData();
        contentData.setContents(messages.stream().map(message -> {
            Content content = new Content();
            content.setRole(message.getRole() == ChatRoleEnum.USER ? "user" : "model");
            content.setParts(Collections.singletonList(new Part(message.getContent())));
            return content;
        }).collect(Collectors.toList()));
        return contentData;
    }
}

