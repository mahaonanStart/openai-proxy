package com.mahaonan.gpt.proxy.chat.glm;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.mahaonan.gpt.proxy.helper.HttpClientPro;
import com.mahaonan.gpt.proxy.helper.JsonUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mahaonan
 */
public class GlmUtils {

    public static final String REFRESH_URL = "https://chatglm.cn/chatglm/backend-api/v1/user/refresh";

    public static void refreshToken() {
        String configFile = getConfigStr();
        Map<String, String> config = JsonUtils.parseToMap(configFile, String.class, String.class);
        if (config == null || !config.containsKey("refresh_token")) {
            throw new RuntimeException("refresh token not found");
        }
        String refreshToken = config.get("refresh_token");
        if (StrUtil.isEmpty(refreshToken)) {
            throw new RuntimeException("refresh token not found");
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + refreshToken);
        String res = HttpClientPro.getInstance().postJson(REFRESH_URL, "", headers, null, null, String.class);
        if (res == null) {
            throw new RuntimeException("refresh token failed");
        }
        String newToken = JsonUtils.strExpression(res, "result.accessToken");
        Map<String, String> map = new HashMap<>();
        map.put("access_token", newToken);
        map.put("refresh_token", refreshToken);
        FileUtil.writeString(JsonUtils.objectToJson(map), System.getProperty("user.dir") + "/glm.json", StandardCharsets.UTF_8);
    }

    public static String getToken() {
        String configFile = getConfigStr();
        return JsonUtils.parseToMap(configFile, String.class, String.class).get("access_token");
    }

    public static String getConfigStr() {
        String userDir = System.getProperty("user.dir");
        return FileUtil.readString(userDir + "/glm.json", StandardCharsets.UTF_8);
    }
}
