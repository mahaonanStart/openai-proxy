package com.mahaonan.gpt.proxy.helper;

import lombok.SneakyThrows;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import static com.mahaonan.gpt.proxy.helper.HttpConstant.SUFFIX_MAP;


public class Encodes {

    public static final String ENCODE_UTF_8 = "UTF-8";

    public static final String TEXT_FILE_SUFFIX = ".txt";
    public static final String MD_FILE_SUFFIX = ".md";
    public static final String PDF_FILE_SUFFIX = ".pdf";
    public static final String WORD_FILE_SUFFIX = ".word";
    public static final String EXCEL_FILE_SUFFIX = ".xlsx";
    public static final String TS_FILE_SUFFIX = ".ts";
    public static final Set<String> TEXT_FILE_SET = new HashSet<>();

    static {
        TEXT_FILE_SET.add(TEXT_FILE_SUFFIX);
        TEXT_FILE_SET.add(MD_FILE_SUFFIX);
        TEXT_FILE_SET.add(PDF_FILE_SUFFIX);
        TEXT_FILE_SET.add(WORD_FILE_SUFFIX);
        TEXT_FILE_SET.add(EXCEL_FILE_SUFFIX);
        TEXT_FILE_SET.add(TS_FILE_SUFFIX);
    }

    public static String urlEncode(String part, String charset) {
        try {
            if(null == charset || null == part) {
                return part;
            } else {
                return URLEncoder.encode(part, charset);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("编码错误");
        }
    }

    @SneakyThrows
    public static String getFileNameFromUrl(String url) {
        boolean isFile = SUFFIX_MAP.values().stream().anyMatch(url::endsWith);
        if (isFile) {
            return URLDecoder.decode(url.substring(url.lastIndexOf("/") + 1), ENCODE_UTF_8);
        }else {
            return null;
        }
    }
}
