package com.mahaonan.gpt.proxy.chat.copilot;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author mahaonan
 */
public class CopilotJob {

    @Scheduled(
            cron = "0 0 3 1/3 * ?"
    )
    private static void updateLatestVersion() {
        String latestVersion = getLatestVSCodeVersion();
        String latestChatVersion = getLatestExtensionVersion("GitHub", "copilot-chat");
        if (latestVersion != null && latestChatVersion != null) {
            CopilotChatSession.vsCodeVersion = latestVersion;
            CopilotChatSession.copilotVersion = latestChatVersion;
        }

        System.out.println("===================配置更新说明========================");
        System.out.println("vscode_version：" + latestVersion);
        System.out.println("copilot_chat_version：" + latestChatVersion);
        System.out.println("======================================================");
    }

    public static String getLatestVSCodeVersion() {
        try {
            URL url = new URL("https://api.github.com/repos/microsoft/vscode/releases/latest");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();

                String output;
                while((output = br.readLine()) != null) {
                    response.append(output);
                }
                conn.disconnect();
                JSONObject jsonObject = new JSONObject(response.toString());
                return jsonObject.getStr("tag_name");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getLatestExtensionVersion(String publisher, String name) {
        try {
            HttpURLConnection conn = (HttpURLConnection)(new URL("https://marketplace.visualstudio.com/_apis/public/gallery/extensionquery")).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json;api-version=6.1-preview.1");
            conn.setDoOutput(true);
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("filters", (new JSONArray()).put((new JSONObject()).put("criteria", (new JSONArray()).put((new JSONObject()).put("filterType", 7).put("value", publisher + "." + name)))));
            jsonRequest.put("flags", 870);
            OutputStream os = conn.getOutputStream();
            os.write(jsonRequest.toString().getBytes());
            os.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();

            String output;
            while((output = br.readLine()) != null) {
                response.append(output);
            }

            conn.disconnect();
            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getJSONArray("results").getJSONObject(0).getJSONArray("extensions").getJSONObject(0).getJSONArray("versions").getJSONObject(0).getStr("version");
        } catch (IOException var9) {
            throw new RuntimeException(var9);
        }
    }
}
