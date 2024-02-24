package com.mahaonan.gpt.proxy.helper;


import cn.hutool.core.util.StrUtil;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HttpClientUtils {

    private static final String PARAMETER_START = "?";

    private static final String PARAMETER_SEPARATOR = "&";

    private static final Pattern PARAMETER_PATTERN = Pattern.compile("[?&]*(.*?)=(.*?)(?=&|$)");

    public static String appendParams(String url, String queryString) {
        return url + (url.contains(PARAMETER_START) ? PARAMETER_SEPARATOR : PARAMETER_START) + queryString;
    }

    /**
     * 按照指定的字符集拼接url和参数
     * @param url
     * @param params
     * @param charset
     * @return
     */
    public static String appendParams(String url, Map<String, String> params, String charset) {
        return appendParams(url, linkParams(params, charset));
    }

    public static String linkParams(Map<String, String> params, String charset) {
        if (null != params) {
            return MapUtils.mapToList(params, "=", v -> Encodes.urlEncode(v, charset)).stream().collect(Collectors.joining("&"));
        }
        return "";
    }

    /**
     * 构造表单数据
     * @param params
     * @return
     */
    public static String buildFormParams(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        params.forEach((k, v) -> {
            sb.append(k).append("=").append(v).append("&");
        });
        return sb.substring(0, sb.length() - 1);
    }

    public static String parseFileNameContentDisposition(String contentDisposition) {
        if (StrUtil.isBlank(contentDisposition)) {
            return null;
        }
        String[] values = contentDisposition.split(";");
        if (values.length == 1) {
            return null;
        }
        String[] property = values[values.length - 1].split("=");
        if ("filename".equals(property[0].trim())) {
            String filename = property[1].replace("\"", "");
            return (filename.substring(filename.lastIndexOf("/") + 1));
        }
        return null;
    }

    public static void main(String[] args) {

    }



}
