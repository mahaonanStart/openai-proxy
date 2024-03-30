package com.mahaonan.gpt.proxy.chat.bito;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author mahaonan
 */
@Data
@Builder
public class BitoRequest implements Serializable {

    private static final long serialVersionUID = 5324344749902347074L;
    private String appInfo;

    private String bitoVersion;

    private String deviceInfo;

    private Integer ideId;

    private String uniqueKey;

    private String prompt;

    private String email;

    @JsonProperty("uId")
    private String uid;

    private String sessionId;

    private String requestId;

    private Integer wsId;

    private Integer bitoUserId;

    private Integer userId;

    private String ideName;

    private String invCode;

    private List<Context> context;

    @JsonProperty("Type")
    private Integer type;

    @JsonProperty("Stream")
    private boolean stream;

    @JsonIgnore
    private String authToken;

    private String outputLanguage;
    private String aiModelType;
    private String agentAPI;
    private String embeddingAPI;
    private String contextAnswerAPI;
    private String method;
    private String topN;
    private String topNThreshold;


    @Data
    public static class Context {
        
        private String question;

        private String answer;
    }

}
