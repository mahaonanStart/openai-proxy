package com.mahaonan.gpt.proxy.controller;

import cn.hutool.core.util.StrUtil;
import com.mahaonan.gpt.proxy.config.properties.GptProxyProperties;
import com.mahaonan.gpt.proxy.constant.BotType;
import com.mahaonan.gpt.proxy.model.GptProxyRequest;
import com.mahaonan.gpt.proxy.service.ChatGptProxyService;
import org.reactivestreams.Publisher;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

/**
 * @author mahaonan
 */
@RestController
public class ChatGptProxyController {

    @Resource
    private ChatGptProxyService gptProxyService;
    @Resource
    private GptProxyProperties gptProxyProperties;


    @RequestMapping(value = "/v1/chat/completions")
    public Publisher<String> chat(@RequestBody GptProxyRequest request, ServerWebExchange exchange) {
        //从请求头中获取
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        String authorization = serverHttpRequest.getHeaders().getFirst("Authorization");
        BotType botType = parseAuth(authorization);
        if (botType == null) {
            return Mono.error(new RuntimeException("Authorization is required"));
        }
        request.setBotType(botType);
        return gptProxyService.chat(request);
    }


    /**
     * 解析token
     */
    private BotType parseAuth(String authorization) {
        if (StrUtil.isEmpty(authorization)) {
            return null;
        }
        String token = authorization.split(" ")[1];
        if (StrUtil.isEmpty(token) || !token.startsWith(gptProxyProperties.getKeyPrefix())) {
            return null;
        }
        String[] tokenSplit = token.split("-");
        return BotType.of(tokenSplit[tokenSplit.length - 1]);
    }


}
