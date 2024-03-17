package com.mahaonan.gpt.proxy.chat.kimi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author mahaonan
 */
@Data
public class KimiPreSignModel {

    private String url;

    @JsonProperty("object_name")
    private String objectName;
}
