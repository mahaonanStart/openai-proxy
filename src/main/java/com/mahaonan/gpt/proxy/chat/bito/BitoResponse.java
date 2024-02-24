package com.mahaonan.gpt.proxy.chat.bito;

import lombok.Data;

import java.io.Serializable;

/**
 * @author mahaonan
 */
@Data
public class BitoResponse implements Serializable {

    private static final long serialVersionUID = 7444216646770380124L;

    private String response;

    private Integer status;

    private String created;

    private String id;
}
