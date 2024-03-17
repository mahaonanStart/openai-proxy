package com.mahaonan.gpt.proxy.chat.kimi.handler;

import com.mahaonan.gpt.proxy.chat.kimi.KimiChatSession;
import com.mahaonan.gpt.proxy.chat.kimi.KimiRequestModel;
import com.mahaonan.gpt.proxy.helper.JsonUtils;
import org.springframework.stereotype.Component;

/**
 * @author mahaonan
 */
@Component
public class CreateChatHandler extends AbstractKimiMessageHandler {

    private static final String CREATE_CHAT_URL = "https://kimi.moonshot.cn/api/chat";

    @Override
    public boolean isMatch(KimiRequestModel requestModel) {
        String question = requestModel.getQuestion();
        return question.startsWith("新建") || question.startsWith("创建");
    }

    @Override
    public void handle(KimiRequestModel requestModel) {
        String chatId = createChat();
        KimiChatSession.currentChatId = chatId;
        KimiChatSession.URL = KimiChatSession.URL.replace("{chatId}", chatId);
    }

    @Override
    public String directReturn() {
        return "会话已新建,开始问答吧";
    }

    private String createChat() {
        String body = "{\"name\":\"未命名会话\",\"is_example\":false}";
        String chatRes = repeatRequestWithResult(CREATE_CHAT_URL, body);
        return JsonUtils.strExpression(chatRes, "id");
    }
}
