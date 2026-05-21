package com.yupi.yupicture.infrastructure.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
@ConfigurationProperties(prefix = "cos.client")
@Data
public class CosClientConfig {

    /**
     * 访问域名（可写完整 URL；若只写域名或 IP，保存外链时会自动补上 https://）
     */
    private String host;

    /**
     * 生成浏览器可直接访问的对象 URL。
     * <p>若 host 为裸 IP（或为空），通常并非 COS 访问域名，此时改用腾讯云标准桶域名。</p>
     */
    public String buildPublicObjectUrl(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            return resolvePublicBaseUrl();
        }
        String base = resolvePublicBaseUrl();
        if (base.isEmpty()) {
            return objectKey;
        }
        String key = objectKey.trim();
        while (key.startsWith("/")) {
            key = key.substring(1);
        }
        if (key.isEmpty()) {
            return base;
        }
        return base + "/" + key;
    }

    private String resolvePublicBaseUrl() {
        if (shouldPreferTencentBucketDomain()) {
            return "https://" + bucket.trim() + ".cos." + region.trim() + ".myqcloud.com";
        }
        return normalizeHostBase();
    }

    private boolean shouldPreferTencentBucketDomain() {
        if (bucket == null || bucket.isEmpty() || region == null || region.isEmpty()) {
            return false;
        }
        if (host == null || host.trim().isEmpty()) {
            return true;
        }
        return isBareIpOrLocalhost(host);
    }

    private static boolean isBareIpOrLocalhost(String rawHost) {
        String h = extractHostname(rawHost);
        if (h.isEmpty()) {
            return false;
        }
        if ("localhost".equalsIgnoreCase(h)) {
            return true;
        }
        return h.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
    }

    private static String extractHostname(String rawHost) {
        String h = rawHost.trim();
        if (h.startsWith("http://")) {
            h = h.substring(7);
        } else if (h.startsWith("https://")) {
            h = h.substring(8);
        }
        int slash = h.indexOf('/');
        if (slash >= 0) {
            h = h.substring(0, slash);
        }
        int colon = h.indexOf(':');
        if (colon >= 0) {
            h = h.substring(0, colon);
        }
        return h;
    }

    private String normalizeHostBase() {
        if (host == null) {
            return "";
        }
        String base = host.trim();
        if (base.isEmpty()) {
            return "";
        }
        if (!base.startsWith("http://") && !base.startsWith("https://")) {
            base = "https://" + base;
        }
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }

    public String normalizePictureAccessUrl(String storedUrl) {
        if (storedUrl == null || storedUrl.isEmpty()) {
            return storedUrl;
        }
        String u = collapseDuplicateSlashAfterAuthority(storedUrl.trim());
        if (!shouldPreferTencentBucketDomain()) {
            return ensureHttpScheme(u);
        }
        String cfgHost = extractHostname(host != null ? host : "");
        if (cfgHost.isEmpty()) {
            return ensureHttpScheme(u);
        }
        try {
            String withScheme = ensureHttpScheme(u);
            withScheme = collapseDuplicateSlashAfterAuthority(withScheme);
            URI uri = URI.create(withScheme);
            String h = uri.getHost();
            if (h != null && h.equalsIgnoreCase(cfgHost)) {
                String path = uri.getRawPath();
                if (path == null || path.isEmpty() || "/".equals(path)) {
                    return resolvePublicBaseUrl();
                }
                return buildPublicObjectUrl(path);
            }
        } catch (IllegalArgumentException ignored) {
        }
        return ensureHttpScheme(u);
    }

    private static String ensureHttpScheme(String u) {
        if (u.startsWith("http://") || u.startsWith("https://")) {
            return u;
        }
        String rest = u.replaceFirst("^/+", "");
        rest = rest.replaceAll("^([^/]+)/{2,}", "$1/");
        return "https://" + rest;
    }

    private static String collapseDuplicateSlashAfterAuthority(String u) {
        if (u == null) {
            return null;
        }
        return u.replaceAll("(https?://[^/?#]+)/{2,}", "$1/");
    }

    /**
     * secretId
     */
    private String secretId;

    /**
     * 密钥（注意不要泄露）
     */
    private String secretKey;

    /**
     * 区域
     */
    private String region;

    /**
     * 桶名
     */
    private String bucket;

    @Bean
    public COSClient cosClient() {
        // 1 初始化用户身份信息（secretId, secretKey）。
        // SECRETID 和 SECRETKEY 请登录访问管理控制台 https://console.cloud.tencent.com/cam/capi 进行查看和管理
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 2 设置 bucket 的地域, COS 地域的简称请参见 https://cloud.tencent.com/document/product/436/6224
        // clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分。
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 这里建议设置使用 https 协议
        // 从 5.6.54 版本开始，默认使用了 https
        clientConfig.setHttpProtocol(HttpProtocol.https);
        // 3 生成 cos 客户端。
        return new COSClient(cred, clientConfig);
    }
}
