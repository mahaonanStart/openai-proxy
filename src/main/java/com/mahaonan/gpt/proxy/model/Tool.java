package com.mahaonan.gpt.proxy.model;

import lombok.Data;

/**
 * @author mahaonan
 */
@Data
public class Tool {

    private String type;
    private Function function;


}
