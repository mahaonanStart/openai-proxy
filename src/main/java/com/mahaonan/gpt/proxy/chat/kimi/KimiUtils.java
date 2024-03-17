package com.mahaonan.gpt.proxy.chat.kimi;

import cn.hutool.core.io.FileUtil;
import com.mahaonan.gpt.proxy.helper.HttpClientPro;
import com.mahaonan.gpt.proxy.helper.JsonUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mahaonan
 */
public class KimiUtils {

    public static final String REFRESH_TOKEN_URL = "https://kimi.moonshot.cn/api/auth/token/refresh";

    public static String refreshToken() {
        String configFile = getConfigStr();
        Map<String, String> config = JsonUtils.parseToMap(configFile, String.class, String.class);
        if (config == null || !config.containsKey("refresh_token")) {
            throw new RuntimeException("refresh token not found");
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.get("refresh_token"));
        headers.put("Origin", "https://kimi.moonshot.cn");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36");
        String res = HttpClientPro.getInstance().get(REFRESH_TOKEN_URL, null, headers, String.class);
        if (res == null) {
            throw new RuntimeException("refresh token failed");
        }
        Map<String, String> map = JsonUtils.parseToMap(res, String.class, String.class);
        String newToken = map.get("access_token");
        FileUtil.writeString(JsonUtils.objectToJson(map), System.getProperty("user.dir") + "/kimi.json", StandardCharsets.UTF_8);
        return newToken;
    }

    public static String getToken() {
        String configFile = getConfigStr();
        return JsonUtils.parseToMap(configFile, String.class, String.class).get("access_token");
    }

    public static String getConfigStr() {
        String userDir = System.getProperty("user.dir");
        return FileUtil.readString(userDir + "/kimi.json", StandardCharsets.UTF_8);
    }
}
