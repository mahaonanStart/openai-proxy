package com.mahaonan.gpt.proxy.config.properties;

import lombok.Data;

/**
 * @author mahaonan
 */
@Data
public class OpenaiProperties {

    private boolean enabled;

    private String baseUrl = "https://api.openai.com";

    private String apiKey;
}
