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

    private String model = "gpt-3.5-turbo";

    private String temperature = "0.7";

    private String presencePenalty = "0.0";
}
