package com.mahaonan.gpt.proxy.chat.copilot;

import cn.hutool.cache.Cache;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.json.JSONObject;
import com.mahaonan.gpt.proxy.chat.BaseChatSession;
import com.mahaonan.gpt.proxy.chat.ChatBot;
import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.config.properties.CopilotProperties;
import com.mahaonan.gpt.proxy.config.properties.GptProxyProperties;
import com.mahaonan.gpt.proxy.helper.HttpClientPro;
import com.mahaonan.gpt.proxy.helper.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author mahaonan
 */
@Component("copilot")
@Slf4j
@ConditionalOnProperty(name = "gpt.proxy.copilot.enabled", havingValue = "true")
public class CopilotChatSession extends BaseChatSession {

    protected CopilotProperties copilotProperties;
    protected String COPILOT_URL = "https://api.githubcopilot.com/chat/completions";
    protected String GET_TOKEN_URL;
    protected String apiKey;
    protected Cache<String, String> KEY_TOKEN_MAP = new TimedCache<>(1000 * 60 * 29);
    protected static final String machineId;
    public static String vsCodeVersion;
    public static String copilotVersion;

    static {
        machineId = generateMachineId();
        vsCodeVersion = CopilotJob.getLatestVSCodeVersion();
        copilotVersion = CopilotJob.getLatestExtensionVersion("GitHub", "copilot-chat");
    }


    public CopilotChatSession(WebClient webClient, GptProxyProperties gptProxyProperties) {
        super(webClient, gptProxyProperties);
        this.copilotProperties = getGptProxyProperties().getCopilot();
        this.GET_TOKEN_URL = copilotProperties.getTokenUrl();
        this.apiKey = copilotProperties.getApiKey();
    }

    @Override
    protected ChatBot setChatBot() {
        return ChatBot.COPILOT_AI;
    }

    @Override
    protected Flux<String> postChat(String question, List<ChatMessage> messages) {
        Map<String, Object> params = buildParams(messages, false);
        String answer = HttpClientPro.getInstance().postJson(COPILOT_URL, JsonUtils.objectToJson(params), buildHeader(), null, null, String.class);
        return Flux.just(answer);
    }

    protected String getTokenX() {
        if (KEY_TOKEN_MAP.containsKey(apiKey)) {
            return KEY_TOKEN_MAP.get(apiKey);
        }
        String token = getCopilotToken(apiKey);
        log.info("get token:{}", token);
        KEY_TOKEN_MAP.put(apiKey, token);
        return token;
    }

    protected Map<String, Object> buildParams(List<ChatMessage> messages, boolean stream) {
        Map<String, Object> params = new HashMap<>();
        params.put("messages", messages);
        params.put("model", copilotProperties.getModel());
        params.put("temperature", copilotProperties.getTemperature());
        params.put("presence_penalty", copilotProperties.getPresencePenalty());
        params.put("stream", stream);
        return params;
    }

    protected Map<String, String> buildHeader() {
        String tokenX = getTokenX();
        Map<String, String> headersMap = new HashMap<>();
        //headersMap.put("Host", "api.githubcopilot.com");
        headersMap.put("Accept-Encoding", "gzip, deflate, br");
        headersMap.put("Accept", "*/*");
        headersMap.put("Authorization", "Bearer " + tokenX);
        headersMap.put("X-Request-Id", UUID.randomUUID().toString());
        headersMap.put("X-Github-Api-Version", "2023-07-07");
        String uuid = UUID.randomUUID().toString();
        headersMap.put("Vscode-Sessionid", uuid + System.currentTimeMillis());
        headersMap.put("vscode-machineid", machineId);
        headersMap.put("Editor-Version", vsCodeVersion);
        headersMap.put("Editor-Plugin-Version", "copilot-chat/" + copilotVersion);
        headersMap.put("Openai-Organization", "github-copilot");
        headersMap.put("Copilot-Integration-Id", "vscode-chat");
        headersMap.put("Openai-Intent", "conversation-panel");
        headersMap.put("User-Agent", "GitHubCopilotChat/" + copilotVersion);
        return headersMap;
    }


    protected static String generateMachineId() {
        try {
            UUID uuid = UUID.randomUUID();
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(uuid.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(255 & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getCopilotToken(String apiKey) {
        Map<String, String> headers = new HashMap<>();
        //headers.put("Host", "api.github.com");
        headers.put("authorization", "token " + apiKey);
        headers.put("Editor-Version", vsCodeVersion);
        headers.put("Editor-Plugin-Version", "copilot-chat/" + copilotVersion);
        headers.put("User-Agent", "GitHubCopilotChat/" + copilotVersion);
        headers.put("Accept", "*/*");
        String res = HttpClientPro.getInstance().post(GET_TOKEN_URL, null, headers, "utf-8", null, String.class);
        JSONObject jsonResponse = new JSONObject(res);
        return jsonResponse.getStr("token");
    }
}
