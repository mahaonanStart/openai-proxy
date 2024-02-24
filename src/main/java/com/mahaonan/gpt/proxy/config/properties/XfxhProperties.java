package com.mahaonan.gpt.proxy.config.properties;

import lombok.Data;

/**
 * 讯飞星火配置
 * @author mahaonan
 */
@Data
public class XfxhProperties {

    private boolean enabled;

    private String url;

    private String appId;

    private String apiKey;

    private String apiSecret;

}
