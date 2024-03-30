package com.mahaonan.gpt.proxy.helper;

import com.mahaonan.gpt.proxy.model.GptProxyRequest;

/**
 * @author mahaonan
 */
public class HttpRequestHolder {

    public static final ThreadLocal<GptProxyRequest> REQUEST_HOLDER = new ThreadLocal<>();

    public static void set(GptProxyRequest request) {
        REQUEST_HOLDER.set(request);
    }

    public static GptProxyRequest get() {
        return REQUEST_HOLDER.get();
    }

    public static void remove() {
        REQUEST_HOLDER.remove();
    }
}
