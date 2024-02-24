package com.mahaonan.gpt.proxy.chat.ali;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author mahaonan
 */
@Data
public class AliChatRequestParameter implements Serializable {
    private static final long serialVersionUID = 5617843482380004747L;

    @JsonProperty("top_p")
    private Double topP;

    @JsonProperty("top_k")
    private Integer topK;

    private Integer seed;

    @JsonProperty("enable_search")
    private Boolean enableSearch;

    public static AliChatRequestParameter buildParameter(Boolean enableSearch) {
        AliChatRequestParameter parameter = new AliChatRequestParameter();
        parameter.setTopP(0.8);
        parameter.setTopK(100);
        //生成随机种子
        parameter.setSeed((int) (Math.random() * 10000));
        parameter.setEnableSearch(enableSearch);
        return parameter;
    }
}
