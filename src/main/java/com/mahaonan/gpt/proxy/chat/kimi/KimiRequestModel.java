package com.mahaonan.gpt.proxy.chat.kimi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mahaonan.gpt.proxy.chat.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author mahaonan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KimiRequestModel {

    @JsonIgnore
    private String question;

    private List<ChatMessage> messages;

    private List<String> refs;

    @JsonProperty("use_search")
    private boolean use_search;

    public KimiRequestModel(List<ChatMessage> messages, List<String> refs, boolean useSearch) {
        this.messages = messages;
        this.use_search = useSearch;
        this.refs = refs;
    }

    public void coverMsg(String msg) {
        this.question = msg;
        this.messages.get(0).setContent(msg);
    }
}
