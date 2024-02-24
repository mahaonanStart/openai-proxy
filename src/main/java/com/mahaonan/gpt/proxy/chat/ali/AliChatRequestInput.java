package com.mahaonan.gpt.proxy.chat.ali;

import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.chat.ChatRoleEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mahaonan
 */
@Data
public class AliChatRequestInput implements Serializable {

    private String prompt;

    private List<History> history;

    @Data
    public static class History {
        private String user;
        private String bot;
    }


    public static AliChatRequestInput build(List<ChatMessage> chatMessages) {
        AliChatRequestInput aliChatRequestInput = new AliChatRequestInput();
        ChatMessage currMessage = chatMessages.remove(chatMessages.size() - 1);
        aliChatRequestInput.setPrompt(currMessage.getContent());
        List<History> histories = new ArrayList<>();
        aliChatRequestInput.setHistory(histories);
        for (int i = 0; i < chatMessages.size(); i++) {
            ChatMessage chatMessage = chatMessages.get(i);
            if (chatMessage.getRole() == ChatRoleEnum.USER) {
                continue;
            }
            History history = new History();
            history.setBot(chatMessage.getContent());
            //判断前一个是否为空
            if (i == 0) {
                history.setUser("你好");
            }else {
                ChatMessage lastMessage = chatMessages.get(i - 1);
                if (lastMessage.getRole() == ChatRoleEnum.USER) {
                    history.setUser(lastMessage.getContent());
                }else {
                    history.setUser("你好");
                }
            }
            histories.add(history);
        }
        return aliChatRequestInput;
    }
}
