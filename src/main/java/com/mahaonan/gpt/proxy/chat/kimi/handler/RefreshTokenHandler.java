package com.mahaonan.gpt.proxy.chat.kimi.handler;

import com.mahaonan.gpt.proxy.chat.kimi.KimiRequestModel;
import org.springframework.stereotype.Component;

/**
 * @author mahaonan
 */
@Component
public class RefreshTokenHandler extends AbstractKimiMessageHandler{
    @Override
    public boolean isMatch(KimiRequestModel requestModel) {
        return requestModel.getQuestion().startsWith("刷新token");
    }

    @Override
    public void handle(KimiRequestModel requestModel) {
        refreshToken();
    }

    @Override
    public String directReturn() {
        return "token已刷新";
    }
}
