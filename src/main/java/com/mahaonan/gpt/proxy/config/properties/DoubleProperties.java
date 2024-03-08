package com.mahaonan.gpt.proxy.config.properties;

import lombok.Data;

/**
 * @author mahaonan
 */
@Data
public class DoubleProperties {

    private boolean enabled;

    private String apiKey;

    private String baseUrl = "https://api.double.bot";

    /**
     * 可选值
     * Claude 3 (Opus)
     * GPT4 Turbo
     */
    private String model;
}
