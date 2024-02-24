package com.mahaonan.gpt.proxy.constant;

import lombok.Getter;

/**
 * @author mahaonan
 */
public enum BotType {

    BITO("bito"),
    OPENAI("openai"),
    ALI_GPT("ali"),
    XFXH_GPT("xfxh"),
    GEMINI_GPT("gemini"),
    //轮询
    ALL("all")
    ;

    @Getter
    private final String name;

    BotType(String name) {
        this.name = name;
    }

    public static BotType of(String name) {
        for (BotType value : BotType.values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }

}
