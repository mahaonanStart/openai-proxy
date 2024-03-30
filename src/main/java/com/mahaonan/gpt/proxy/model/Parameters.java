package com.mahaonan.gpt.proxy.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author mahaonan
 */
@Data
@NoArgsConstructor
public class Parameters {

    private String type;
    private Map<String, Map<String, Object>> properties;
    private List<String> required;
}
