package com.mahaonan.gpt.proxy.config.properties;

import lombok.Data;

/**
 * @author mahaonan
 */
@Data
public class BitoProperties {

    private boolean enabled;

    private String url = "https://bitoai.bito.co/ai/v2/chat/?processSilently=true";

    private String headerAuthorization;

    private Integer bitoUserId;

    private String email;

    private String uId;

    private Integer wsId;

    private String requestId;

    private String sessionId;
}
