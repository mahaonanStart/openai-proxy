package com.mahaonan.gpt.proxy.chat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * @author mahaonan
 */
public enum ChatRoleEnum {

    SYSTEM("system", "系统"),
    USER("user", "用户"),
    ASSISTANT("assistant", "助手");

    @JsonValue
    @Getter
    private final String code;
    @Getter
    private final String desc;
    ChatRoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static ChatRoleEnum getEnum(String code) {
        for (ChatRoleEnum enumItem : ChatRoleEnum.values()) {
            if (enumItem.getCode().equals(code)) {
                return enumItem;
            }
        }
        return null;
    }

}
