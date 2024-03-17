package com.mahaonan.gpt.proxy.chat.kimi.handler;

import com.mahaonan.gpt.proxy.chat.kimi.KimiRequestModel;
import org.springframework.stereotype.Component;

/**
 * @author mahaonan
 */
@Component
public class WebsiteHandler extends AbstractKimiMessageHandler{

    private final String urlTemplate = "<url id=\"\" type=\"url\" status=\"\" title=\"\" wc=\"\">{URL}</url>";

    @Override
    public boolean isMatch(KimiRequestModel requestModel) {
        return requestModel.getQuestion().startsWith("https:") || requestModel.getQuestion().startsWith("http:");
    }

    @Override
    public void handle(KimiRequestModel requestModel) {
        String question = urlTemplate.replace("{URL}", requestModel.getQuestion());
        requestModel.coverMsg(question);
    }
}
