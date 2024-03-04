package com.mahaonan.gpt.proxy.chat;

import lombok.Getter;

/**
 * @author mahaonan
 */
@Getter
public enum ChatBot {

    OPEN_AI(1, "opeani", false),
    OPEN_STREAM_AI(2, "openaiStream", true),
    BITO_AI(3, "bito", false),
    BITO_STEAM_AI(4, "bitoStream", true),
    XFXH_AI(5, "xfxh", false),
    XFXH_STREAM_AI(6, "xfxhStream", true),
    ALI_AI(7, "ali", false),
    ALI_STEAM_AI(8, "aliStream", true),
    GEMINI_AI(9, "gemini", false),
    GEMINI_STREAM_AI(10, "geminiStream", true),
    COPILOT_AI(20, "copilot", false),
    COPILOT_STREAM_AI(21, "copilotStream", true),
    ;

    private final Integer code;

    private final String name;

    private final boolean stream;

    ChatBot(Integer code, String name, boolean stream) {
        this.code = code;
        this.name = name;
        this.stream = stream;
    }

    public static ChatBot getEnum(Integer code) {
        for (ChatBot enumItem : ChatBot.values()) {
            if (enumItem.getCode().equals(code)) {
                return enumItem;
            }
        }
        return null;
    }

    public static boolean isOpenAi(ChatBot chatBot) {
        return chatBot == ChatBot.OPEN_AI
                || chatBot == ChatBot.OPEN_STREAM_AI
                || chatBot == ChatBot.COPILOT_AI
                || chatBot == ChatBot.COPILOT_STREAM_AI;
    }


}
