package com.roominspection.backend.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.roominspection.backend.util.EncryptionUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * 门禁接口认证服务
 * 支持双向SSL认证，用于与门禁系统进行安全通信
 */
@Service
public class DoorAccessAuthService {

    private static final Logger logger = LoggerFactory.getLogger(DoorAccessAuthService.class);

    @Autowired
    @Qualifier("twoWaySslHttpClient")
    private CloseableHttpClient httpClient;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Value("${access-control.hikvision.api-url:}")
    private String hikvisionApiUrl;

    @Value("${access-control.dahua.api-url:}")
    private String dahuaApiUrl;

    /**
     * 生成门禁接口签名
     * 用于门禁接口的双向认证和防篡改
     *
     * @param apiSecret  API密钥
     * @param timestamp  时间戳
     * @param nonce      随机数
     * @param params     请求参数
     * @return 签名字符串
     */
    public String generateSignature(String apiSecret, String timestamp, String nonce, String params) {
        // 按照门禁系统规定的签名算法生成签名
        // 这里使用示例：MD5(apiSecret + timestamp + nonce + params)
        String signData = apiSecret + timestamp + nonce + params;
        return encryptionUtil.sha256Hash(signData);
    }

    /**
     * 下发门禁权限（支持双向认证）
     *
     * @param systemType   门禁系统类型（hikvision、dahua）
     * @param staffId      值班人员ID
     * @param cardNumber   卡号
     * @param deviceId     门禁设备ID
     * @param startTime    开始时间（Unix时间戳）
     * @param endTime      结束时间（Unix时间戳）
     * @return 下发结果
     */
    public JSONObject grantPermission(String systemType, Long staffId, String cardNumber,
                                       String deviceId, Long startTime, Long endTime) {
        try {
            String apiUrl = getApiUrl(systemType);
            if (apiUrl == null) {
                return errorResult("不支持的门禁系统类型");
            }

            // 构建请求参数
            JSONObject params = new JSONObject();
            params.put("staffId", staffId);
            params.put("cardNumber", cardNumber);
            params.put("deviceId", deviceId);
            params.put("startTime", startTime);
            params.put("endTime", endTime);

            // 生成签名信息
            String timestamp = String.valueOf(System.currentTimeMillis());
            String nonce = generateNonce();
            String apiSecret = getApiSecret(systemType);
            String signature = generateSignature(apiSecret, timestamp, nonce, params.toJSONString());

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("timestamp", timestamp);
            requestBody.put("nonce", nonce);
            requestBody.put("signature", signature);
            requestBody.put("data", params);

            // 发送请求
            HttpPost httpPost = new HttpPost(apiUrl + "/api/v1/permission/grant");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("X-API-VERSION", "1.0");

            // 添加自定义认证头
            httpPost.setHeader("X-API-KEY", apiSecret);

            StringEntity entity = new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);

            // 执行请求（使用双向SSL认证的HTTP客户端）
            HttpResponse response = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            logger.info("门禁权限下发响应: {}", responseBody);

            // 解析响应
            JSONObject result = JSON.parseObject(responseBody);

            // 验证响应签名
            if (!verifyResponseSignature(result, apiSecret)) {
                return errorResult("响应签名验证失败");
            }

            return result;

        } catch (Exception e) {
            logger.error("门禁权限下发失败", e);
            return errorResult("门禁权限下发失败: " + e.getMessage());
        }
    }

    /**
     * 回收门禁权限（支持双向认证）
     *
     * @param systemType  门禁系统类型
     * @param staffId     值班人员ID
     * @param deviceId    门禁设备ID
     * @return 回收结果
     */
    public JSONObject revokePermission(String systemType, Long staffId, String deviceId) {
        try {
            String apiUrl = getApiUrl(systemType);
            if (apiUrl == null) {
                return errorResult("不支持的门禁系统类型");
            }

            // 构建请求参数
            JSONObject params = new JSONObject();
            params.put("staffId", staffId);
            params.put("deviceId", deviceId);

            // 生成签名信息
            String timestamp = String.valueOf(System.currentTimeMillis());
            String nonce = generateNonce();
            String apiSecret = getApiSecret(systemType);
            String signature = generateSignature(apiSecret, timestamp, nonce, params.toJSONString());

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("timestamp", timestamp);
            requestBody.put("nonce", nonce);
            requestBody.put("signature", signature);
            requestBody.put("data", params);

            // 发送请求
            HttpPost httpPost = new HttpPost(apiUrl + "/api/v1/permission/revoke");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("X-API-VERSION", "1.0");
            httpPost.setHeader("X-API-KEY", apiSecret);

            StringEntity entity = new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);

            // 执行请求
            HttpResponse response = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            logger.info("门禁权限回收响应: {}", responseBody);

            // 解析响应
            JSONObject result = JSON.parseObject(responseBody);

            // 验证响应签名
            if (!verifyResponseSignature(result, apiSecret)) {
                return errorResult("响应签名验证失败");
            }

            return result;

        } catch (Exception e) {
            logger.error("门禁权限回收失败", e);
            return errorResult("门禁权限回收失败: " + e.getMessage());
        }
    }

    /**
     * 获取门禁API URL
     */
    private String getApiUrl(String systemType) {
        switch (systemType.toLowerCase()) {
            case "hikvision":
                return hikvisionApiUrl;
            case "dahua":
                return dahuaApiUrl;
            default:
                return null;
        }
    }

    /**
     * 获取门禁API密钥
     */
    private String getApiSecret(String systemType) {
        // 从配置或数据库获取对应系统的API密钥
        switch (systemType.toLowerCase()) {
            case "hikvision":
                return "hikvision-api-secret"; // 应从配置读取
            case "dahua":
                return "dahua-api-secret"; // 应从配置读取
            default:
                return "";
        }
    }

    /**
     * 生成随机数
     */
    private String generateNonce() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 验证响应签名
     */
    private boolean verifyResponseSignature(JSONObject response, String apiSecret) {
        try {
            String signature = response.getString("signature");
            String timestamp = response.getString("timestamp");
            String nonce = response.getString("nonce");
            String data = response.getJSONObject("data").toJSONString();

            String expectedSignature = generateSignature(apiSecret, timestamp, nonce, data);

            return expectedSignature.equals(signature);
        } catch (Exception e) {
            logger.error("验证响应签名失败", e);
            return false;
        }
    }

    /**
     * 构建错误结果
     */
    private JSONObject errorResult(String message) {
        JSONObject result = new JSONObject();
        result.put("success", false);
        result.put("message", message);
        return result;
    }
}
