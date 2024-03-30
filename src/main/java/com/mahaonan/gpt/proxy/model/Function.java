package com.mahaonan.gpt.proxy.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author mahaonan
 */
@Data
@NoArgsConstructor
public class Function {

    private String url;
    private String name;
    private String description;
    private Parameters parameters;
}
