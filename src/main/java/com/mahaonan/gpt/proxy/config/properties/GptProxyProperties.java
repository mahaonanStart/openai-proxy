package com.mahaonan.gpt.proxy.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

/**
 * @author mahaonan
 */
@Data
@ConfigurationProperties(prefix = "gpt.proxy")
@Configuration
public class GptProxyProperties {

    private String keyPrefix = "gpt-proxy";

    private boolean printLog = true;

    @NestedConfigurationProperty
    private XfxhProperties xfxh = new XfxhProperties();

    @NestedConfigurationProperty
    private AliProperties ali = new AliProperties();

    @NestedConfigurationProperty
    private GeminiProperties gemini = new GeminiProperties();

    @NestedConfigurationProperty
    private BitoProperties bito = new BitoProperties();

    @NestedConfigurationProperty
    private OpenaiProperties openai = new OpenaiProperties();

    @NestedConfigurationProperty
    private CopilotProperties copilot = new CopilotProperties();

}
