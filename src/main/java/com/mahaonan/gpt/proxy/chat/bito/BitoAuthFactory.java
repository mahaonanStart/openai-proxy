package com.mahaonan.gpt.proxy.chat.bito;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mahaonan.gpt.proxy.helper.JsonUtils;
import lombok.Data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mahaonan
 */
public class BitoAuthFactory {

    private static final List<BitoAuth> bitoCache = new ArrayList<>();

    private static final AtomicInteger index = new AtomicInteger(0);

    public static BitoAuth getBitoAuth() {
        if (CollectionUtil.isNotEmpty(bitoCache)) {
            return bitoCache.get(index.getAndIncrement() % bitoCache.size());
        }
        InputStream inputStream = loadFile("bito/bitoAuth.json");
        String bitoAuthJson = IoUtil.read(inputStream, "utf-8");
        bitoCache.addAll(JsonUtils.parseToList(bitoAuthJson, BitoAuth.class));
        return bitoCache.get(index.getAndIncrement() % bitoCache.size());
    }


    public static InputStream loadFile(String filePath) {
        //首先从classpath寻找
        InputStream inputStream = new ClassPathResource(filePath).getStream();
        if (inputStream != null) {
            return inputStream;
        }
        //然后从系统路径找
        File file = new File(filePath);
        if (file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException ignored) {
            }
        }
        throw new RuntimeException(filePath + "未找到");
    }

    @Data
    public static class BitoAuth {

        private String headerAuthorization;
        private String email;
        private Integer bitoUserId;
        @JsonProperty("uId")
        private String uid;
        private Integer wsId;
        private String requestId;
        private String sessionId;
    }
}
