package com.mahaonan.gpt.proxy.config.properties;

import lombok.Data;

/**
 * @author mahaonan
 */
@Data
public class GeminiProperties {

    private boolean enabled;
    /**
     * 基础url
     */
    private String baseUrl = "https://generativelanguage.googleapis.com";

    private String key;
}
