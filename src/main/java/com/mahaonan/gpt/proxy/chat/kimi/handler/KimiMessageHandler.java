package com.mahaonan.gpt.proxy.chat.kimi.handler;

import com.mahaonan.gpt.proxy.chat.kimi.KimiRequestModel;

/**
 * 处理kimi消息
 * @author mahaonan
 */
public interface KimiMessageHandler {

    boolean isMatch(KimiRequestModel requestModel);

    void handle(KimiRequestModel requestModel);

    default String directReturn() {
        return "";
    }
}
