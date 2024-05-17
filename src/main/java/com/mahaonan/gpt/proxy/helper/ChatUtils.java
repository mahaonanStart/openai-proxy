package com.mahaonan.gpt.proxy.helper;

import com.mahaonan.gpt.proxy.chat.model.ChatGptAnswer;

import java.util.Collections;
import java.util.UUID;

/**
 * @author mahaonan
 */
public class ChatUtils {

    public static String buildStopMsg() {
        String id = "chatcmpl-" + UUID.randomUUID().toString().replace("-", "");
        ChatGptAnswer answer = new ChatGptAnswer();
        answer.setId(id);
        answer.setObject("chat.completion.chunk");
        answer.setCreated(System.currentTimeMillis());
        answer.setModel("gpt-35-turbo");
        ChatGptAnswer.Choices choices = new ChatGptAnswer.Choices();
        choices.setIndex(0);
        ChatGptAnswer.Message message = new ChatGptAnswer.Message();
        choices.setDelta(message);
        choices.setFinish_reason("stop");
        answer.setChoices(Collections.singletonList(choices));
        return JsonUtils.objectToJson(answer);
    }


    public static String buildGptStreamMsg(String originMsg, boolean stream) {
        String id = "chatcmpl-" + UUID.randomUUID().toString().replace("-", "");
        if (stream) {
            ChatGptAnswer answer = new ChatGptAnswer();
            answer.setId(id);
            answer.setObject("chat.completion.chunk");
            answer.setCreated(System.currentTimeMillis());
            answer.setModel("gpt-35-turbo");
            ChatGptAnswer.Choices choices = new ChatGptAnswer.Choices();
            choices.setIndex(0);
            ChatGptAnswer.Message message = new ChatGptAnswer.Message();
            message.setRole("assistant");
            message.setContent(originMsg);
            choices.setDelta(message);
            answer.setChoices(Collections.singletonList(choices));
            return JsonUtils.objectToJson(answer);
        }
        ChatGptAnswer answer = new ChatGptAnswer();
        ChatGptAnswer.Choices choices = new ChatGptAnswer.Choices();
        ChatGptAnswer.Message message = new ChatGptAnswer.Message();
        answer.setId(id);
        answer.setObject("chat.completion");
        answer.setCreated(System.currentTimeMillis());
        answer.setModel("gpt-35-turbo");
        message.setRole("assistant");
        message.setContent(originMsg);
        choices.setMessage(message);
        choices.setFinish_reason("stop");
        answer.setChoices(Collections.singletonList(choices));
        return JsonUtils.objectToJson(answer);
    }

}
