package com.blog.common.config;

import feign.RequestInterceptor;
import feign.Response;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Feign Client 全局配置
 * <p>
 * 超时通过 application.properties 配置：
 * spring.cloud.openfeign.client.config.default.connect-timeout=3000
 * spring.cloud.openfeign.client.config.default.read-timeout=10000
 * <p>
 * 重试通过本类的 Retryer Bean 配置
 * <p>
 * 日志级别通过 application.properties 配置：
 * spring.cloud.openfeign.client.config.default.logger-level=FULL
 */
@Configuration
public class FeignClientConfig {

    private static final Logger log = LoggerFactory.getLogger(FeignClientConfig.class);

    /**
     * 重试配置
     * <p>
     * 参数说明：
     * - period：首次重试间隔 100ms
     * - maxPeriod：最大重试间隔 1000ms（指数退避上限）
     * - maxAttempts：最大重试次数 3（含首次请求，即失败后重试 2 次）
     * <p>
     * 注意：Feign Retryer 仅在 IOException（网络超时/连接拒绝）时触发，
     * HTTP 5xx 错误不会自动重试（除非自定义 ErrorDecoder 抛 RetryableException）
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 1000, 3);
    }

    /**
     * 错误解码器
     * <p>
     * 对 502/503/504 网关错误抛出 RetryableException，使 Retryer 能触发重试
     * 其他错误码正常传递给调用方
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            int status = response.status();
            log.warn("[Feign] 调用失败: method={}, status={}, url={}",
                    methodKey, status, response.request().url());
            if (status == 502 || status == 503 || status == 504) {
                return new feign.RetryableException(
                        status,
                        "服务暂时不可用 (" + status + ")，将重试",
                        response.request().httpMethod(),
                        new java.util.Date(),
                        response.request()
                );
            }
            return new ErrorDecoder.Default().decode(methodKey, response);
        };
    }

    /**
     * 请求拦截器：打印每次 Feign 请求的详细信息
     * <p>
     * 日志内容：HTTP 方法、URL、请求头、请求体
     */
    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return template -> {
            log.info("[Feign] 发起请求: {} {}",
                    template.method(),
                    template.url());
            if (log.isDebugEnabled()) {
                template.headers().forEach((key, values) ->
                        log.debug("[Feign]   请求头: {}={}", key, values));
                if (template.body() != null) {
                    log.debug("[Feign]   请求体: {}", new String(template.body(), StandardCharsets.UTF_8));
                }
            }
        };
    }

    /**
     * 响应拦截器：打印 Feign 响应详情
     * <p>
     * 日志内容：HTTP 状态码、耗时、响应体（截断到 2000 字符防止日志爆炸）
     */
    @Bean
    public feign.codec.Decoder feignResponseDecoder() {
        return new ResponseDecoder();
    }

    /**
     * 自定义响应解码器，在反序列化前打印响应日志
     */
    private static class ResponseDecoder implements feign.codec.Decoder {

        private static final Logger decLog = LoggerFactory.getLogger(ResponseDecoder.class);
        private final feign.codec.Decoder delegate = new feign.codec.Decoder.Default();

        @Override
        public Object decode(Response response, java.lang.reflect.Type type) throws IOException, feign.codec.DecodeException {
            long start = System.currentTimeMillis();
            String method = response.request().method();
            String url = response.request().url();
            int status = response.status();

            // 读取响应体副本（不影响原始流）
            String body = "";
            if (response.body() != null) {
                try (BufferedReader reader = new BufferedReader(
                        new java.io.InputStreamReader(response.body().asInputStream(), StandardCharsets.UTF_8))) {
                    body = reader.lines().collect(Collectors.joining("\n"));
                }
                // 截断到 2000 字符
                if (body.length() > 2000) {
                    body = body.substring(0, 2000) + "...(truncated)";
                }
            }

            long elapsed = System.currentTimeMillis() - start;
            if (status >= 200 && status < 300) {
                decLog.info("[Feign] 响应成功: {} {} -> {} (耗时 {}ms)", method, url, status, elapsed);
                if (decLog.isDebugEnabled() && !body.isEmpty()) {
                    decLog.debug("[Feign]   响应体: {}", body);
                }
            } else {
                decLog.warn("[Feign] 响应异常: {} {} -> {} (耗时 {}ms) body={}", method, url, status, elapsed, body);
            }

            // 重新构造 Response（因为 body 流已被消费）
            Response rebuilt = response.toBuilder()
                    .body(body.getBytes(StandardCharsets.UTF_8))
                    .build();
            return delegate.decode(rebuilt, type);
        }
    }
}
