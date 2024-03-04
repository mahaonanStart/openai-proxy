package com.mahaonan.gpt.proxy.config.properties;

import lombok.Data;

/**
 * @author mahaonan
 */
@Data
public class CopilotProperties {

    private boolean enabled;

    /**
     * 获取token的url
     */
    private String tokenUrl = "https://api.github.com/copilot_internal/v2/token";

    /**
     * copilot apiKey,ghu或者gho开头的
     */
    private String apiKey;

    private String model = "gpt-3.5-turbo";

    private Double temperature = 0.7;

    private Double presencePenalty = 0.0;
}
