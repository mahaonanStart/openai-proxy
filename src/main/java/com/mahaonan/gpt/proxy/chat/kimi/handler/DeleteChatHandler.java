package com.mahaonan.gpt.proxy.chat.kimi.handler;

import com.mahaonan.gpt.proxy.chat.kimi.KimiChatSession;
import com.mahaonan.gpt.proxy.chat.kimi.KimiRequestModel;
import com.mahaonan.gpt.proxy.chat.kimi.KimiUtils;
import com.mahaonan.gpt.proxy.helper.HttpClientPro;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author mahaonan
 */
@Component
public class DeleteChatHandler extends AbstractKimiMessageHandler{

    @Override
    public boolean isMatch(KimiRequestModel requestModel) {
        return requestModel.getQuestion().startsWith("删除");
    }

    @Override
    public void handle(KimiRequestModel requestModel) {
        deleteChat();
    }

    @Override
    public String directReturn() {
        return "会话已删除";
    }

    private void deleteChat() {
        if (KimiChatSession.currentChatId == null) {
            throw new RuntimeException("会话不存在");
        }
        String deleteUrl = "https://kimi.moonshot.cn/api/chat/" + KimiChatSession.currentChatId;
        Map<String, String> headers = buildHeaders(KimiUtils.getToken());
        headers.put("Content-Type", "application/json");
        HttpClientPro.getInstance().delete(deleteUrl, headers);
        KimiChatSession.URL = KimiChatSession.URL.replace(KimiChatSession.currentChatId, "{chatId}");
    }
}
