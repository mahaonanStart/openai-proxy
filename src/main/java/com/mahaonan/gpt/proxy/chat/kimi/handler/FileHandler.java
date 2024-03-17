package com.mahaonan.gpt.proxy.chat.kimi.handler;

import com.mahaonan.gpt.proxy.chat.kimi.KimiPreSignModel;
import com.mahaonan.gpt.proxy.chat.kimi.KimiRequestModel;
import com.mahaonan.gpt.proxy.chat.kimi.KimiUtils;
import com.mahaonan.gpt.proxy.helper.HttpClientPro;
import com.mahaonan.gpt.proxy.helper.JsonUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mahaonan
 */
@Component
public class FileHandler extends AbstractKimiMessageHandler{

    public static final String PRE_SIGN_FILE_URL = "https://kimi.moonshot.cn/api/pre-sign-url";
    public static final String UPLOAD_FILE_URL = "https://kimi.moonshot.cn/api/file";
    private Pattern filePattern = Pattern.compile("(https?://[\\w_-]+(?:(?:\\.[\\w_-]+)+)(?:[\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?)");

    @Override
    public boolean isMatch(KimiRequestModel requestModel) {
        return requestModel.getQuestion().startsWith("文件");
    }

    @Override
    public void handle(KimiRequestModel requestModel) {
        String question = requestModel.getQuestion().substring(2);
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
        requestModel.coverMsg(question);
        //从文件链接中获取文件名
        String fileName = "temp" + System.currentTimeMillis() + fileUrl.substring(fileUrl.lastIndexOf("."));
        KimiPreSignModel signModel = preSignFile(fileName);
        uploadOss(signModel, fileUrl);
        String fileId = uploadFile(fileName, signModel);
        requestModel.getRefs().add(fileId);
    }

    private KimiPreSignModel preSignFile(String fileName) {
        String body = "{\"action\":\"file\",\"name\":\"" + fileName + "\"}";
        String res = repeatRequestWithResult(PRE_SIGN_FILE_URL, body);
        return JsonUtils.parse(res, KimiPreSignModel.class);
    }

    private void uploadOss(KimiPreSignModel signModel, String fileUrl) {
        String url = signModel.getUrl();
        String token = KimiUtils.getToken();
        Map<String, String> headers = buildHeaders(token);
        HttpClientPro.getInstance().putFile(url, headers, fileUrl);
    }

    private String uploadFile(String fileName, KimiPreSignModel signModel) {
        String body = "{\"type\":\"file\",\"name\":\"" + fileName + "\",\"object_name\":\"" + signModel.getObjectName() + "\"}";
        String res = repeatRequestWithResult(UPLOAD_FILE_URL, body);
        return JsonUtils.strExpression(res, "id");
    }

}
