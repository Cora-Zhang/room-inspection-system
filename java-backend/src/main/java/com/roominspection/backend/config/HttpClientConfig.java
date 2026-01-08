package com.roominspection.backend.config;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

/**
 * HTTP客户端配置
 * 支持双向SSL认证，用于门禁接口等需要双向认证的场景
 */
@Configuration
public class HttpClientConfig {

    @Value("${security.ssl.key-store:}")
    private String keyStorePath;

    @Value("${security.ssl.key-store-password:}")
    private String keyStorePassword;

    @Value("${security.ssl.trust-store:}")
    private String trustStorePath;

    @Value("${security.ssl.trust-store-password:}")
    private String trustStorePassword;

    /**
     * 创建支持双向SSL认证的HTTP客户端
     * 用于与门禁系统进行双向认证通信
     */
    @Bean("twoWaySslHttpClient")
    public CloseableHttpClient twoWaySslHttpClient() {
        try {
            // 构建SSL上下文
            SSLContextBuilder sslBuilder = SSLContextBuilder.create();

            // 加载密钥库（用于客户端认证）
            if (keyStorePath != null && !keyStorePath.isEmpty()) {
                sslBuilder.loadKeyMaterial(
                        new java.io.File(keyStorePath),
                        keyStorePassword.toCharArray(),
                        keyStorePassword.toCharArray()
                );
            }

            // 加载信任库（用于验证服务端证书）
            if (trustStorePath != null && !trustStorePath.isEmpty()) {
                sslBuilder.loadTrustMaterial(
                        new java.io.File(trustStorePath),
                        trustStorePassword.toCharArray()
                );
            } else {
                // 如果没有指定信任库，信任所有证书（仅用于开发环境）
                sslBuilder.loadTrustMaterial(null, (chains, authType) -> true);
            }

            SSLContext sslContext = sslBuilder.build();

            // 创建SSL连接工厂
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    NoopHostnameVerifier.INSTANCE  // 生产环境应使用正确的主机名验证
            );

            // 创建HTTP客户端
            return HttpClients.custom()
                    .setSSLSocketFactory(sslSocketFactory)
                    .setMaxConnTotal(200)
                    .setMaxConnPerRoute(50)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("创建双向SSL HTTP客户端失败", e);
        }
    }

    /**
     * 创建常规HTTP客户端
     * 用于普通HTTP请求
     */
    @Bean("defaultHttpClient")
    public CloseableHttpClient defaultHttpClient() {
        return HttpClients.custom()
                .setMaxConnTotal(200)
                .setMaxConnPerRoute(50)
                .build();
    }
}
