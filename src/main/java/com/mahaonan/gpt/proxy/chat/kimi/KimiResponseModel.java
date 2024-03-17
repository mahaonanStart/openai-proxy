package com.mahaonan.gpt.proxy.chat.kimi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mahaonan
 */
@Data
public class KimiResponseModel {

    private String text = "";

    private String event;

    private Msg msg;


    @Data
    public static class Msg {
        /**
         * start: 正在尝试为您在互联网搜索相关资料...
         * start_res: 开始获取资料...
         * get_res: 找到了第successNum篇资料: [title](url)
         * done: 搜索完毕,共找到urlList.size()篇资料
         */
        private String type;
        private Integer successNum;
        private String title;
        private String url;
        @JsonProperty("url_list")
        private List<UrlList> urlList = new ArrayList<>();
    }

    @Data
    public static class UrlList {
        private String title;
        private String url;
    }

}
