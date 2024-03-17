package com.mahaonan.gpt.proxy.config;

import com.mahaonan.gpt.proxy.helper.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author mahaonan
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler<T> {

    @ExceptionHandler(Exception.class)
    public ResponseResult<T> handler(Exception e) {
        log.error("系统异常", e);
        return ResponseResult.error(901);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseResult<T> handlerRuntime(RuntimeException e) {
        log.error(e.getMessage());
        return new ResponseResult<>(-1, e.getMessage());
    }
}
