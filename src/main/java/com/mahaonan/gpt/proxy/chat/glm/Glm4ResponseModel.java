package com.mahaonan.gpt.proxy.chat.glm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author mahaonan
 */
@Data
public class Glm4ResponseModel {

    private String id;
    @JsonProperty("conversation_id")
    private String conversationId;
    @JsonProperty("assistant_id")
    private String assistantId;
    private List<Part> parts;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("meta_data")
    private MetaData metaData;
    private String status;
    @JsonProperty("last_error")
    private Object lastError;

    @Data
    public static class Part {
        private String id;
        @JsonProperty("logic_id")
        private String logicId;
        private String role;
        private List<Content> content;
        private String model;
        private String recipient;
        @JsonProperty("created_at")
        private String createdAt;
        @JsonProperty("meta_data")
        private MetaData metaData;
        private String status;
    }

    @Data
    public static class Content {
        private String type;
        private String text;
        private List<Image> image;
        private String code;
        private String content;
        @JsonProperty("tool_calls")
        private ToolCall toolCalls;
        private String status;
    }

    @Data
    public static class MetaData {
        private String channel;
        @JsonProperty("metadata_list")
        private List<MetadataList> metadataList;
        @JsonProperty("mention_conversation_id")
        private String mentionConversationId;
        @JsonProperty("is_test")
        private boolean isTest;
        @JsonProperty("input_question_type")
        private String inputQuestionType;
        @JsonProperty("draft_id")
        private String draftId;
        private String ip;
    }

    @Data
    public static class Image {
        @JsonProperty("image_url")
        private String imageUrl;
    }

    @Data
    public static class ToolCall {
        private String name;
        private String arguments;
    }

    @Data
    public static class MetadataList {
        private String type;
        private String title;
        private String url;
        private String text;
    }
}
