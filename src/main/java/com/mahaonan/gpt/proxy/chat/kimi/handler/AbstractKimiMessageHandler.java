package com.mahaonan.gpt.proxy.chat.kimi.handler;

import com.mahaonan.gpt.proxy.chat.kimi.KimiUtils;
import com.mahaonan.gpt.proxy.helper.HttpClientPro;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mahaonan
 */
public abstract class AbstractKimiMessageHandler implements KimiMessageHandler{

    public Map<String, String> buildHeaders(String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        headers.put("Origin", "https://kimi.moonshot.cn");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36");
        return headers;
    }

    public String refreshToken() {
        return KimiUtils.refreshToken();
    }

    public String repeatRequestWithResult(String url, String body) {
        String token = KimiUtils.getToken();
        Map<String, String> headers = buildHeaders(token);
        String res = HttpClientPro.getInstance().postJson(url, body, headers, null, null, String.class);
        if (res == null) {
            token = refreshToken();
            headers = buildHeaders(token);
            res = HttpClientPro.getInstance().postJson(url, body, headers, null, null, String.class);
        }
        if (res == null) {
            throw new RuntimeException("request failed");
        }
        return res;
    }
}
