package com.mahaonan.gpt.proxy.chat.glm;

import cn.hutool.core.collection.ListUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author mahaonan
 */
@Data
public class Glm4RequestModel {
    private static final long serialVersionUID = 7696064891660640244L;

    @JsonProperty("assistant_id")
    private String assistantId;

    @JsonProperty("conversation_id")
    private String conversationId;

    @JsonProperty("meta_data")
    private MetaData metaData;
    private List<Message> messages;



    @Data
    public static class MetaData {
        @JsonProperty("mention_conversation_id")
        private String mentionConversationId = "";
        @JsonProperty("is_test")
        private boolean isTest;
        @JsonProperty("input_question_type")
        private String inputQuestionType;
        private String channel = "";
        @JsonProperty("draft_id")
        private String draftId = "";
    }

    @Data
    public static class Message {
        private String role;
        private List<Content> content;
    }

    @Data
    public static class Content {
        private String type;
        private String text;
        private List<GlmFile> file;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GlmFile {
        @JsonProperty("file_id")
        private String fileId;
        @JsonProperty("file_url")
        private String fileUrl;
        @JsonProperty("file_name")
        private String fileName;
        @JsonProperty("file_size")
        private Integer fileSize;
    }

    public static Glm4RequestModel buildModel(String question, String conversationId) {
        Glm4RequestModel model = new Glm4RequestModel();
        model.setAssistantId("65940acff94777010aa6b796");
        model.setConversationId(conversationId);
        MetaData metaData = new MetaData();
        metaData.setMentionConversationId("");
        metaData.setTest(false);
        metaData.setInputQuestionType("xxxx");
        metaData.setChannel("");
        metaData.setDraftId("");
        model.setMetaData(metaData);
        Message message = new Message();
        message.setRole("user");
        Content content = new Content();
        content.setType("text");
        content.setText(question);
        message.setContent(ListUtil.toList(content));
        model.setMessages(ListUtil.toList(message));
        return model;
    }

    public static Glm4RequestModel buildModel(String question, String conversationId, Glm4FileUploadResponse fileUploadResponse) {
        Glm4FileUploadResponse.Result responseResult = fileUploadResponse.getResult();
        Glm4RequestModel requestModel = buildModel(question, conversationId);
        Content content = new Content();
        content.setType("file");
        content.setFile(List.of(new GlmFile(responseResult.getFileId(), responseResult.getFileUrl(), responseResult.getFileName(), fileUploadResponse.getFileLength())));
        requestModel.getMessages().get(0).getContent().add(content);
        return requestModel;
    }
}
