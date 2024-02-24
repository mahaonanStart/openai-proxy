package com.mahaonan.gpt.proxy.helper;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ResponseResult<E> {

    private static final long serialVersionUID = -968662622056751643L;

    private int code;

    private String message;

    private E result;

    public ResponseResult() {

    }

    public ResponseResult(int code) {
        this.code = code;
    }

    public ResponseResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseResult(int code, E result) {
        this.code = code;
        this.result = result;
    }

    public ResponseResult(int code, String message, E result) {
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public static <E> ResponseResult<E> ok() {
        return new ResponseResult<>(0, "success");
    }

    public static <E> ResponseResult<E> error(int code) {
        return new ResponseResult<>(code, "error");
    }

    public static <E> ResponseResult<E> ok(E data) {
        return new ResponseResult<>(0, "sucess", data);
    }

}
