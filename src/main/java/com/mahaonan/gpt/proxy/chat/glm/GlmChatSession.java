package com.mahaonan.gpt.proxy.chat.glm;

import cn.hutool.core.collection.CollectionUtil;
import com.mahaonan.gpt.proxy.chat.BaseChatSession;
import com.mahaonan.gpt.proxy.chat.ChatBot;
import com.mahaonan.gpt.proxy.chat.ChatMessage;
import com.mahaonan.gpt.proxy.chat.kimi.KimiException;
import com.mahaonan.gpt.proxy.config.properties.GptProxyProperties;
import com.mahaonan.gpt.proxy.helper.HttpClientPro;
import com.mahaonan.gpt.proxy.helper.JsonUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mahaonan
 */
@Component("glmStream")
public class GlmChatSession extends BaseChatSession {

    public static final String CHAT_URL = "https://chatglm.cn/chatglm/backend-api/assistant/stream";

    public static String CONVERSATION_id = "";

    public static final String FILE_UPLOAD_URL = "https://chatglm.cn/chatglm/backend-api/assistant/file_upload";

    public static final String DELETE_URL = "https://chatglm.cn/chatglm/backend-api/assistant/conversation/delete";

    private Pattern filePattern = Pattern.compile("(https?://[\\w_-]+(?:(?:\\.[\\w_-]+)+)(?:[\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?)");

    public GlmChatSession(WebClient webClient, GptProxyProperties properties) {
        super(webClient, properties);
    }

    @Override
    protected ChatBot setChatBot() {
        return ChatBot.GLM_STREAM_AI;
    }

    private Flux<String> postChatWithToken(Glm4RequestModel requestModel, int retryCount) {
        if (retryCount <= 0) {
            // 如果重试次数已经用完，就不再重试，而是直接抛出异常
            return Flux.error(new RuntimeException("Retry limit exceeded"));
        }
        StringBuilder prevMsg = new StringBuilder();
        StringBuilder codePrevMsg = new StringBuilder();
        return getWebClient().post()
                .uri(CHAT_URL)
                .headers(httpHeaders -> {
                    buildHeaders().forEach(httpHeaders::add);
                })
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(JsonUtils.objectToJson(requestModel))
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> Mono.error(new KimiException(clientResponse.statusCode().value(), clientResponse.statusCode().getReasonPhrase())))
                .bodyToFlux(String.class).map(data -> {
                    Glm4ResponseModel response = JsonUtils.parse(data, Glm4ResponseModel.class);
                    if (response == null) {
                        return "";
                    }
                    if (CONVERSATION_id.isEmpty()) {
                        CONVERSATION_id = response.getConversationId();
                    }
                    if ("finish".equals(response.getStatus())) {
                        return "[DONE]";
                    }
                    List<Glm4ResponseModel.Part> parts = response.getParts();
                    if (parts == null || parts.isEmpty()) {
                        return "";
                    }
                    Glm4ResponseModel.Part part = parts.get(0);
                    String totalStatus = part.getStatus();
                    if ("finish".equals(totalStatus)) {
                        return "";
                    }
                    List<Glm4ResponseModel.Content> contents = part.getContent();
                    if (CollectionUtil.isEmpty(contents)) {
                        return "";
                    }
                    Glm4ResponseModel.Content content = contents.get(0);
                    String status = content.getStatus();
                    if ("finish".equals(status)) {
                        return "";
                    }
                    switch (content.getType()) {
                        case "text":
                            String text = content.getText();
                            text = text.substring(prevMsg.length());
                            prevMsg.append(text);
                            return text;
                        case "tool_calls":
                            Glm4ResponseModel.ToolCall toolCall = content.getToolCalls();
                            String name = toolCall.getName();
                            if("cogview".equals(name)) {
                                return "正在画图中,请稍后\n";
                            }
                            return "";
                        case "image":
                            List<Glm4ResponseModel.Image> imageList = content.getImage();
                            if (CollectionUtil.isEmpty(imageList)) {
                                return "";
                            }
                            StringBuilder imgBuilder = new StringBuilder();
                            for (Glm4ResponseModel.Image image : imageList) {
                                imgBuilder.append(String.format("![](%s)", image.getImageUrl())).append("\n");
                            }
                            return imgBuilder.toString();
                        case "browser_result":
                            Glm4ResponseModel.MetaData browserMetaData = part.getMetaData();
                            if (browserMetaData == null) {
                                return "";
                            }
                            List<Glm4ResponseModel.MetadataList> metadataList = browserMetaData.getMetadataList();
                            if (CollectionUtil.isEmpty(metadataList)) {
                                return "";
                            }
                            StringBuilder browserBuilder = new StringBuilder("联网查询资料如下:\n");
                            for (Glm4ResponseModel.MetadataList metadata : metadataList) {
                                browserBuilder.append(String.format("[%s](%s)", metadata.getTitle(), metadata.getUrl())).append("\n");
                            }
                            return browserBuilder.toString();
                        case "code":
                            StringBuilder codeBuilder = new StringBuilder();
                            if (codePrevMsg.length() == 0) {
                                codeBuilder.append("\n```").append(part.getRecipient()).append("\n");
                            }
                            String code = content.getCode();
                            code = code.substring(codePrevMsg.length());
                            codePrevMsg.append(code);
                            codeBuilder.append(code);
                            return codeBuilder.toString();
                        case "execution_output":
                            String outputBuilder = "\n结果如下:\n" + content.getContent() + "\n";
                            prevMsg.setLength(0);
                            return outputBuilder;
                    }
                    return "";
                })
                .onErrorResume(e -> {
                    if (e instanceof KimiException) {
                        KimiException exception = (KimiException) e;
                        if (exception.getCode() == 422) {
                            GlmUtils.refreshToken();
                            return postChatWithToken(requestModel, retryCount - 1);
                        }
                    }
                    return Flux.error(e);
                });
    }

    @Override
    protected Flux<String> postChat(String question, List<ChatMessage> messages) {
        if (question.startsWith("新建") || question.startsWith("创建")) {
            question = "你好";
        }else if (question.startsWith("删除")) {
            deleteConversation();
            CONVERSATION_id = "";
            return Flux.just("会话已删除");
        } else if (question.startsWith("刷新会话")) {
            CONVERSATION_id = "";
            return Flux.just("会话已刷新");
        } else if (question.startsWith("刷新token")){
            GlmUtils.refreshToken();
            return Flux.just("token已刷新");
        }else if (CONVERSATION_id.isEmpty()) {
            return Flux.just("请先新建会话");
        }
        Glm4RequestModel requestModel;
        if (question.startsWith("文档") || question.startsWith("文件")) {
            question = question.substring(2);
            //提取出 http或者https链接形式的文字,替换为""
            Matcher matcher = filePattern.matcher(question);
            // 提取并打印所有找到的URL
            String fileUrl = "";
            while (matcher.find()) {
                fileUrl = matcher.group();
            }
            if (fileUrl.isEmpty()) {
                throw new RuntimeException("文件链接格式不正确");
            }
            // 将找到的URL替换为空字符串
            question = matcher.replaceAll("");
            Glm4FileUploadResponse fileUploadResponse = uploadFile(fileUrl);
            if (fileUploadResponse == null) {
                return Flux.just("文件上传失败");
            }
            requestModel = Glm4RequestModel.buildModel(question, CONVERSATION_id, fileUploadResponse);
        }else {
            requestModel = Glm4RequestModel.buildModel(question, CONVERSATION_id);
        }
        return postChatWithToken(requestModel, 2);
    }

    private void deleteConversation() {
        if (CONVERSATION_id.isEmpty()) {
            return;
        }
        String response = HttpClientPro.getInstance().postJson(DELETE_URL, JsonUtils.objectToJson(new HashMap<>() {{
            put("assistant_id", "65940acff94777010aa6b796");
            put("conversation_id", CONVERSATION_id);
        }}), buildHeaders(), null, null, String.class);
        if (!"success".equals(JsonUtils.strExpression(response, "message"))) {
            throw new RuntimeException("删除会话失败");
        }
    }


    protected Glm4FileUploadResponse uploadFile(String fileUrl) {
        return HttpClientPro.getInstance().postFile(FILE_UPLOAD_URL, buildHeaders(), fileUrl, Glm4FileUploadResponse.class);
    }

    protected Map<String, String> buildHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + GlmUtils.getToken());
        headers.put("Referer", "https://chatglm.cn/main/alltoolsdetail");
        headers.put("Origin", "https://chatglm.cn");
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
        headers.put("Sec-Ch-Ua", "\"Chromium\";v=\"122\", \"Not(A:Brand\";v=\"24\", \"Google Chrome\";v=\"122\"");
        headers.put("Sec-Ch-Ua-Platform", "\"MacOS\"");
        headers.put("Accept", "*/*");
        headers.put("Platform", "MacOS");
        headers.put("Sec-Ch-Ua-Mobile", "?0");
        return headers;
    }

    @Override
    protected boolean isEnd(StringBuilder totalMsg, String currMsg) {
        return "[DONE]".equals(currMsg);
    }
}
