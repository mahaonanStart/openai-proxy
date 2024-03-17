package com.mahaonan.gpt.proxy.chat.kimi;

import lombok.Getter;

/**
 * @author mahaonan
 */
@Getter
public enum EventType {

    PING("ping"),
    SEARCH_PLUS("search_plus"),
    RESP("resp"),
    CMPL("cmpl"),
    ;

    private final String value;

    EventType(String value) {
        this.value = value;
    }
}
