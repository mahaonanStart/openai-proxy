package com.mahaonan.gpt.proxy.config;

import com.mahaonan.gpt.proxy.chat.BaseChatSession;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mahaonan
 */
@Component
public class ApplicationConfig implements ApplicationRunner {

    @Resource
    private List<BaseChatSession> allChatSession;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        allChatSession
                .forEach(session -> {
                    int weight = session.weight();
                    String botName = session.getChatBot().getName();
                    if (botName.endsWith("stream")) {
                        return;
                    }
                    for (int i = 0; i < weight; i++) {
                        PollingChatSessionHolder.pollingChatBotNames.add(botName);
                    }
                });

    }


    public static class PollingChatSessionHolder {
        private static final List<String> pollingChatBotNames = new ArrayList<>();
        private static final AtomicInteger index = new AtomicInteger(0);

        public static String getBotName() {
            return pollingChatBotNames.get(index.getAndIncrement() % pollingChatBotNames.size());
        }
    }
}
