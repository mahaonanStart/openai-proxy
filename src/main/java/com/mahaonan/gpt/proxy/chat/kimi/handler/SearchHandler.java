package com.mahaonan.gpt.proxy.chat.kimi.handler;

import com.mahaonan.gpt.proxy.chat.kimi.KimiRequestModel;
import org.springframework.stereotype.Component;

/**
 * @author mahaonan
 */
@Component
public class SearchHandler extends AbstractKimiMessageHandler{
    @Override
    public boolean isMatch(KimiRequestModel requestModel) {
        return requestModel.getQuestion().startsWith("联网");
    }

    @Override
    public void handle(KimiRequestModel requestModel) {
        requestModel.coverMsg(requestModel.getQuestion().substring(2));
        requestModel.setUse_search(true);
    }
}
