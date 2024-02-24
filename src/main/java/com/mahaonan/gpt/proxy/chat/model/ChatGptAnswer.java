package com.mahaonan.gpt.proxy.chat.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author mahaonan
 */
@Data
public class ChatGptAnswer implements Serializable {

    private static final long serialVersionUID = -825366440903545307L;
    
    private String id;
    private String object;
    private Long created;
    private String model;

    private List<Choices> choices;

    @Data
    public static class Choices implements Serializable {
        private static final long serialVersionUID = 3556102499457123622L;
        private int index;
        /**
         * stream为false时返回
         */
        private Message message;
        /**
         * stream为true时返回
         */
        private Message delta;
        private String finish_reason;
    }

    @Data
    public static class Message implements Serializable {
        private static final long serialVersionUID = -2112388477526711802L;
        private String role;
        private String content;
    }
}
