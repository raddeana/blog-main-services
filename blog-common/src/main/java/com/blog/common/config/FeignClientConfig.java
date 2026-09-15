package com.blog.common.config;

import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign Client 全局配置
 * <p>
 * 超时通过 application.properties 配置：
 * spring.cloud.openfeign.client.config.default.connect-timeout=3000
 * spring.cloud.openfeign.client.config.default.read-timeout=10000
 * <p>
 * 重试通过本类的 Retryer Bean 配置
 */
@Configuration
public class FeignClientConfig {

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
}
