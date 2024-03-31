package com.mahaonan.gpt.proxy.chat.glm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author mahaonan
 */
@Data
public class Glm4FileUploadResponse {
    private String message;

    private Integer status;

    public Result result;

    private Integer fileLength;

    @Data
    public static class Result {
        private String content;
        @JsonProperty("file_id")
        private String fileId;
        @JsonProperty("file_url")
        private String fileUrl;
        @JsonProperty("file_name")
        private String fileName;

    }
}
