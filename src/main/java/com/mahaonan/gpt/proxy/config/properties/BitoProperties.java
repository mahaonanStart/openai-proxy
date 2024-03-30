package com.mahaonan.gpt.proxy.config.properties;

import lombok.Data;

/**
 * @author mahaonan
 */
@Data
public class BitoProperties {

    private boolean enabled;

    private String url = "https://bitoai.bito.co/ai/v2/chat/?processSilently=true";

    private String authToken;

    private Integer bitoUserId;

    private String email;

    private Integer wsId;
}
