package com.mahaonan.gpt.proxy.chat.ali;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author mahaonan
 */
@Data
public class AliChatResponse implements Serializable {
    private static final long serialVersionUID = 6871243709087677261L;

    private OutputData output;
    private UsageData usage;
    @JsonProperty("request_id")
    private String requestId;

    @Data
    public static class OutputData {
        @JsonProperty("finish_reason")
        private String finishReason;
        private String text;
    }

    @Data
    public static class UsageData {
        @JsonProperty("input_tokens")
        private int inputTokens;
        @JsonProperty("output_tokens")
        private int outputTokens;
    }
}
