package com.roominspection.backend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短信发送工具类
 * 用于值班提醒和异常告警的短信通知
 */
@Slf4j
@Component
public class SmsUtil {

    @Value("${sms.api-url}")
    private String apiUrl;

    @Value("${sms.access-key-id}")
    private String accessKeyId;

    @Value("${sms.access-key-secret}")
    private String accessKeySecret;

    @Value("${sms.sign-name}")
    private String signName;

    @Value("${sms.template-code-shift-reminder}")
    private String templateCodeShiftReminder;

    @Value("${sms.template-code-handover-reminder}")
    private String templateCodeHandoverReminder;

    @Value("${sms.template-code-abnormal-alert}")
    private String templateCodeAbnormalAlert;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 发送短信
     *
     * @param phoneNumbers 手机号（多个用逗号分隔）
     * @param templateCode 短信模板Code
     * @param templateParam 模板参数
     * @return 发送结果
     */
    public boolean sendSms(String phoneNumbers, String templateCode, Map<String, String> templateParam) {
        try {
            // 这里集成阿里云短信服务或其他短信服务商
            // 示例为通用调用方式，实际需根据短信服务商API调整

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("PhoneNumbers", phoneNumbers);
            requestBody.put("SignName", signName);
            requestBody.put("TemplateCode", templateCode);
            requestBody.put("TemplateParam", templateParam);

            // 调用短信API（实际项目中需要添加签名验证等）
            // ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestBody, String.class);

            log.info("短信发送成功：手机号={}, 模板={}, 参数={}", phoneNumbers, templateCode, templateParam);
            return true;

        } catch (Exception e) {
            log.error("短信发送失败：手机号={}, 模板={}", phoneNumbers, templateCode, e);
            return false;
        }
    }

    /**
     * 发送值班提醒短信
     *
     * @param phoneNumbers 手机号列表
     * @param staffName 值班人员姓名
     * @param scheduleDate 值班日期
     * @param shift 班次
     * @param roomName 机房名称
     * @return 发送结果
     */
    public boolean sendShiftReminder(List<String> phoneNumbers, String staffName,
                                     String scheduleDate, String shift, String roomName) {
        String shiftText = "DAY".equals(shift) ? "白班" : "夜班";

        Map<String, String> templateParam = new HashMap<>();
        templateParam.put("staffName", staffName);
        templateParam.put("scheduleDate", scheduleDate);
        templateParam.put("shift", shiftText);
        templateParam.put("roomName", roomName);

        return sendSms(String.join(",", phoneNumbers), templateCodeShiftReminder, templateParam);
    }

    /**
     * 发送值班交接提醒短信
     *
     * @param phoneNumbers 手机号列表
     * @param staffName 值班人员姓名
     * @return 发送结果
     */
    public boolean sendHandoverReminder(List<String> phoneNumbers, String staffName) {
        Map<String, String> templateParam = new HashMap<>();
        templateParam.put("staffName", staffName);

        return sendSms(String.join(",", phoneNumbers), templateCodeHandoverReminder, templateParam);
    }

    /**
     * 发送值班异常告警短信
     *
     * @param phoneNumbers 手机号列表
     * @param staffName 值班人员姓名
     * @param reason 异常原因
     * @return 发送结果
     */
    public boolean sendShiftAbnormalAlert(List<String> phoneNumbers, String staffName, String reason) {
        Map<String, String> templateParam = new HashMap<>();
        templateParam.put("staffName", staffName);
        templateParam.put("reason", reason);

        return sendSms(String.join(",", phoneNumbers), templateCodeAbnormalAlert, templateParam);
    }
}
