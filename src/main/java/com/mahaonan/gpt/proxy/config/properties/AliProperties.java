package com.mahaonan.gpt.proxy.config.properties;

import lombok.Data;

/**
 * 阿里通义千问配置
 * @author mahaonan
 */
@Data
public class AliProperties {

    private boolean enabled;

    private String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    private String apiKey;

    private String model = "qwen-max";
}
