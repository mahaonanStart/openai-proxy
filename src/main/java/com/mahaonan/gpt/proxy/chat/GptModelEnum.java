package com.mahaonan.gpt.proxy.chat;

import lombok.Getter;

/**
 * @author mahaonan
 */
public enum GptModelEnum {

    GPT_35_TURBO("gpt-3.5-turbo"),
    GPT_35_TURBO_16K("gpt-3.5-turbo-16k"),
    GPT_35_TURBO_0613("gpt-3.5-turbo-0613"),
    GPT_35_TURBO_16K_0613("gpt-3.5-turbo-16k-0613"),
    GPT_4("gpt-4"),
    GPT_4_0613("gpt-4-0613"),
    GPT_4_32K("gpt-4-32k"),
    GPT_4_32K_0613("gpt-4-32k-0613"),
    ;

    @Getter
    public final String model;

    GptModelEnum(String model) {
        this.model = model;
    }

    public GptModelEnum of(String model) {
        for (GptModelEnum value : values()) {
            if (value.model.equals(model)) {
                return value;
            }
        }
        return GptModelEnum.GPT_35_TURBO_0613;
    }
}
