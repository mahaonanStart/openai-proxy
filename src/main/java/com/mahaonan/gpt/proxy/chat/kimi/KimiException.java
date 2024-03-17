package com.mahaonan.gpt.proxy.chat.kimi;

import lombok.Getter;

/**
 * @author mahaonan
 */
public class KimiException extends RuntimeException{

    @Getter
    private final int code;

    public KimiException(int code, String message) {
        super(message);
        this.code = code;
    }
}
