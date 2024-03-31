package com.mahaonan.gpt.proxy.helper;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpUtil;
import com.mahaonan.gpt.proxy.chat.glm.Glm4FileUploadResponse;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

/**
 * @author mahaonan
 */
@Slf4j
public class HttpClientPro {

    private static final String DEFAULT_CHARSET = "UTF-8";

    private HttpClient httpClient;

    private static volatile HttpClientPro INSTANCE;

    private static volatile HttpClientPro PROXY_INSTANCE;

    private void init(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public static HttpClientPro getInstance() {
        if (INSTANCE == null) {
            synchronized (HttpClientPro.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Builder().build();
                }
            }
        }
        return INSTANCE;
    }

    public static HttpClientPro getGlobalProxyInstance(String host, int port) {
        if (PROXY_INSTANCE == null) {
            synchronized (HttpClientPro.class) {
                if (PROXY_INSTANCE == null) {
                    PROXY_INSTANCE = new Builder().proxy(host, port).build();
                }
            }
        }
        return PROXY_INSTANCE;
    }

    public static HttpClientPro getMultiProxyInstance(String host, int port) {
        return new Builder().proxy(host, port).build();
    }

    public <T> T get(String url, Class<T> clazz) {
        return get(url, null, null, DEFAULT_CHARSET, null, clazz);
    }

    public <T> T get(String url, Map<String, String> params, Class<T> clazz) {
        return get(url, params, null, DEFAULT_CHARSET, null, clazz);
    }

    public <T> T get(String url, Map<String, String> params, Map<String, String> headers, Class<T> clazz) {
        return get(url, params, headers, DEFAULT_CHARSET, null, clazz);
    }

    public <T> T get(String url, Map<String, String> params, Map<String, String> headers, String sendCharset, String responseCharset, Class<T> clazz) {
        Objects.requireNonNull(url);
        Objects.requireNonNull(clazz);
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        if (MapUtil.isNotEmpty(headers)) {
            headers.forEach(builder::header);
        }
        if (MapUtil.isNotEmpty(params)) {
            url = HttpClientUtils.appendParams(url, params, sendCharset);
        }
        HttpRequest request = builder.uri(URI.create(url)).GET().build();
        try {
            HttpResponse<String> httpResponse = httpClient.send(request, responseCharset == null ?
                    HttpResponse.BodyHandlers.ofString() : (responseInfo) -> HttpResponse.BodySubscribers.ofString(Charset.forName(responseCharset)));
            int statusCode = httpResponse.statusCode();
            if (statusCode == 200) {
                return clazz == String.class ? (T) httpResponse.body() : JsonUtils.parse(httpResponse.body(), clazz);
            }
            log.error("请求url:{}出错,headers:{},params:{},sendCharset:{},statusCode:{}", url, headers, params, sendCharset, statusCode);
        } catch (Exception e) {
            log.error("请求url:{}出错,headers:{},params:{},sendCharset:{}", url, headers, params, sendCharset, e);
        }
        return null;
    }

    public <T> T post(String url, Class<T> clazz) {
        return post(url, null, null, DEFAULT_CHARSET, null, clazz);
    }

    public <T> T post(String url, Map<String, Object> params, Class<T> clazz) {
        return post(url, params, null, DEFAULT_CHARSET, null, clazz);
    }

    public <T> T post(String url, Map<String, Object> params, Map<String, String> headers, Class<T> clazz) {
        return post(url, params, headers, DEFAULT_CHARSET, null, clazz);
    }

    public <T> T post(String url, Map<String, Object> params, Map<String, String> headers, String sendCharset, String responseCharset, Class<T> clazz) {
        Objects.requireNonNull(url);
        Objects.requireNonNull(clazz);
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
        if (MapUtil.isNotEmpty(headers)) {
            headers.forEach(builder::header);
            if (!headers.containsKey("Content-Type")) {
                builder.header("Content-Type", ContentType.FORM_URLENCODED.getValue());
            }
        }
        if (MapUtil.isNotEmpty(params)) {
            if (sendCharset == null) {
                sendCharset = DEFAULT_CHARSET;
            }
            builder.POST(HttpRequest.BodyPublishers.ofString(HttpClientUtils.buildFormParams(params), Charset.forName(sendCharset)));
        }
        HttpRequest request = builder.build();
        try {
            HttpResponse<String> httpResponse = httpClient.send(request, responseCharset == null ?
                    HttpResponse.BodyHandlers.ofString() : (responseInfo) -> HttpResponse.BodySubscribers.ofString(Charset.forName(responseCharset)));
            int statusCode = httpResponse.statusCode();
            if (statusCode == 200) {
                return clazz == String.class ? (T) httpResponse.body() : JsonUtils.parse(httpResponse.body(), clazz);
            }
            log.error("请求url:{}出错,headers:{},params:{},sendCharset:{},statusCode:{}", url, headers, params, sendCharset, statusCode);
        } catch (Exception e) {
            log.error("请求url:{}出错,headers:{},params:{},sendCharset:{}", url, headers, params, sendCharset, e);
        }
        return null;
    }

    public <T> T postJson(String url, String body, Map<String, String> headers, String sendCharset, String responseCharset, Class<T> clazz) {
        Objects.requireNonNull(url);
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
        if (MapUtil.isEmpty(headers)) {
            headers = new HashMap<>();
        }
        headers.forEach(builder::header);
        if (!headers.containsKey("Content-Type")) {
            builder.header("Content-Type", ContentType.JSON.getValue());
        }
        if (sendCharset == null) {
            sendCharset = DEFAULT_CHARSET;
        }
        HttpRequest request = builder.POST(HttpRequest.BodyPublishers.ofString(body, Charset.forName(sendCharset))).build();
        try {
            HttpResponse<String> httpResponse = httpClient.send(request, responseCharset == null ?
                    HttpResponse.BodyHandlers.ofString() : (responseInfo) -> HttpResponse.BodySubscribers.ofString(Charset.forName(responseCharset)));
            int statusCode = httpResponse.statusCode();
            if (statusCode == 200) {
                return clazz == String.class ? (T) httpResponse.body() : JsonUtils.parse(httpResponse.body(), clazz);
            }
            log.error("请求url:{}出错,headers:{},params:{},sendCharset:{},statusCode:{}, error:{}", url, headers, body, sendCharset, statusCode, httpResponse.body());
        } catch (Exception e) {
            log.error("请求url:{}出错,headers:{},params:{},sendCharset:{}", url, headers, body, sendCharset, e);
        }
        return null;
    }

    public void postJsonStream(String url, String body, Map<String, String> headers, String sendCharset, Flow.Subscriber<? super List<ByteBuffer>> subscriber) {
        Objects.requireNonNull(url);
        Objects.requireNonNull(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
        if (MapUtil.isNotEmpty(headers)) {
            headers.forEach(builder::header);
            if (!headers.containsKey("Content-Type")) {
                builder.header("Content-Type", ContentType.JSON.getValue());
            }
        }
        if (sendCharset == null) {
            sendCharset = DEFAULT_CHARSET;
        }
        HttpRequest request = builder.POST(HttpRequest.BodyPublishers.ofString(body, Charset.forName(sendCharset))).build();
        CompletableFuture<HttpResponse<Void>> futureResponse = httpClient.sendAsync(request,
                HttpResponse.BodyHandlers.fromSubscriber(subscriber));
        futureResponse.join();
    }

    public void delete(String url, Map<String, String> headers) {
        Objects.requireNonNull(url);
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).DELETE();
        if (MapUtil.isNotEmpty(headers)) {
            headers.forEach(builder::header);
        }
        HttpRequest request = builder.build();
        try {
            HttpResponse<Void> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            int statusCode = httpResponse.statusCode();
            if (statusCode != 200) {
                log.error("请求url:{}出错,headers:{},statusCode:{}", url, headers, statusCode);
            }
        } catch (Exception e) {
            log.error("请求url:{}出错,headers:{}", url, headers, e);
        }
    }

    public void putFile(String url, Map<String, String> headers, String fileUrl) {
        Objects.requireNonNull(url);
        Objects.requireNonNull(fileUrl);
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
        if (MapUtil.isNotEmpty(headers)) {
            headers.forEach(builder::header);
        }
        //先从远程地址获取文件
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HttpUtil.download(fileUrl, outputStream, true);
        HttpRequest request = builder.PUT(HttpRequest.BodyPublishers.ofByteArray(outputStream.toByteArray())).build();
        try {
            HttpResponse<Void> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            int statusCode = httpResponse.statusCode();
            if (statusCode != 200) {
                log.error("请求url:{}出错,headers:{},statusCode:{}", url, headers, statusCode);
            }
        } catch (Exception e) {
            log.error("请求url:{}出错,headers:{}", url, headers, e);
        }
    }

    public <T> T postFile(String url, Map<String, String> headers, String fileUrl, Class<T> clazz) {
        Objects.requireNonNull(url);
        Objects.requireNonNull(fileUrl);
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
        if (MapUtil.isNotEmpty(headers)) {
            headers.forEach(builder::header);
        }

        //先从远程地址获取文件
        // 从远程地址下载文件到字节数组
        byte[] fileContent = downloadFile(fileUrl);
        if (fileContent == null) {
            System.err.println("文件下载失败");
            return null;
        }
        int fileSize = fileContent.length;
        //从fileUrl中提取文件名
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

        // 构建multipart/form-data请求体
        HttpRequest.BodyPublisher bodyPublisher = ofMimeMultipartData(fileContent, "file", fileName);
        builder.header("Content-Type", "multipart/form-data; boundary=" + boundary);
        HttpRequest request = builder
                .POST(bodyPublisher).build();

        try {
            HttpResponse<String> httpResponse = httpClient.send(request, (responseInfo) -> HttpResponse.BodySubscribers.ofString(Charset.forName(DEFAULT_CHARSET)));
            int statusCode = httpResponse.statusCode();
            if (statusCode != 200) {
                log.error("请求url:{}出错,headers:{},statusCode:{}", url, headers, statusCode);
            }
            T t = clazz == String.class ? (T) httpResponse.body() : JsonUtils.parse(httpResponse.body(), clazz);
            if (t instanceof Glm4FileUploadResponse) {
                ((Glm4FileUploadResponse) t).setFileLength(fileSize);
            }
            return t;
        } catch (Exception e) {
            log.error("请求url:{}出错,headers:{}", url, headers, e);
        }
        return null;
    }

    private byte[] downloadFile(String fileUrl) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(fileUrl)).GET().build();
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final String boundary = "Boundary-" + System.currentTimeMillis();

    private static HttpRequest.BodyPublisher ofMimeMultipartData(byte[] fileContent, String paramName, String fileName) {
        //根据文件名后缀,设置不同的content-type
        var byteArrays = new java.util.ArrayList<byte[]>();
        var separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + paramName + "\"; filename=\"" + fileName  + "\"\r\nContent-Type: " + HttpUtil.getMimeType(fileName) + "\r\n\r\n").getBytes(StandardCharsets.UTF_8);
        byteArrays.add(separator);
        byteArrays.add(fileContent);
        var terminator = ("\r\n--" + boundary + "--").getBytes(StandardCharsets.UTF_8);
        byteArrays.add(terminator);
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }



    public static class Builder {

        private Integer connectTimeout = 10000;

        private HttpClient.Version version = HttpClient.Version.HTTP_2;

        private ProxySelector proxySelector = ProxySelector.getDefault();

        public HttpClientPro build() {
            SSLContext sslContext;
            try {
                sslContext = createIgnoreVerifySSL();
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
            String[] supportedProtocols;
            supportedProtocols = new String[] { "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2" };
            log.debug("supportedProtocols: {}", String.join(", ", supportedProtocols));
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(connectTimeout))
                    .sslContext(sslContext)
                    .proxy(proxySelector)
                    .version(version).build();
            HttpClientPro httpClientPro = new HttpClientPro();
            httpClientPro.init(client);
            return httpClientPro;
        }

        private SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
            // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
            X509TrustManager trustManager = new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[] { trustManager }, null);
            System.setProperty("jdk.internal.httpclient.disableHostnameVerification","true");//取消主机名验证
            return sc;
        }


        public Builder connectTimeout(Integer connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder version(HttpClient.Version version) {
            this.version = version;
            return this;
        }

        public Builder proxy(String hostname, int port) {
            this.proxySelector = ProxySelector.of(new InetSocketAddress(hostname, port));
            return this;
        }
    }

}
