package com.roominspection.backend.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 钉钉同步工具类
 * 用于钉钉多维表同步和消息推送
 */
@Slf4j
@Component
public class DingTalkSyncUtil {

    @Value("${dingtalk.app-key}")
    private String appKey;

    @Value("${dingtalk.app-secret}")
    private String appSecret;

    @Value("${dingtalk.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 获取访问令牌
     *
     * @return 访问令牌
     */
    public String getAccessToken() {
        String url = baseUrl + "/v1.0/oauth2/accessToken";

        JSONObject requestBody = new JSONObject();
        requestBody.put("appKey", appKey);
        requestBody.put("appSecret", appSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JSONObject result = JSON.parseObject(response.getBody());
            return result.getString("accessToken");
        }

        throw new RuntimeException("获取钉钉访问令牌失败");
    }

    /**
     * 从钉钉多维表同步值班表
     *
     * @param appId 多维表应用ID
     * @param tableId 表ID
     * @return 同步结果
     */
    public Map<String, Object> syncFromDingTalk(String appId, String tableId) {
        try {
            String accessToken = getAccessToken();

            // 获取多维表数据
            String url = baseUrl + "/v1.0/yonbip/bip/dingtalk/group/group/getMemberPage";
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-acs-dingtalk-access-token", accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject result = JSON.parseObject(response.getBody());
                List<Map<String, Object>> shiftList = parseDingTalkShiftData(result);

                Map<String, Object> syncResult = new HashMap<>();
                syncResult.put("success", true);
                syncResult.put("data", shiftList);
                syncResult.put("syncTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                syncResult.put("count", shiftList.size());

                return syncResult;
            }

            throw new RuntimeException("从钉钉多维表同步失败");

        } catch (Exception e) {
            log.error("钉钉同步异常", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }

    /**
     * 解析钉钉多维表数据
     *
     * @param dingTalkData 钉钉返回数据
     * @return 解析后的值班列表
     */
    private List<Map<String, Object>> parseDingTalkShiftData(JSONObject dingTalkData) {
        List<Map<String, Object>> shiftList = new ArrayList<>();

        // 根据实际钉钉多维表结构解析数据
        // 这里需要根据实际的钉钉多维表字段映射关系进行调整
        if (dingTalkData.containsKey("data")) {
            JSONObject data = dingTalkData.getJSONObject("data");
            if (data.containsKey("list")) {
                List<JSONObject> items = data.getList("list", JSONObject.class);

                for (JSONObject item : items) {
                    Map<String, Object> shift = new HashMap<>();
                    // 示例字段映射，需要根据实际钉钉表结构修改
                    shift.put("scheduleDate", item.getString("field_date"));
                    shift.put("shift", item.getString("field_shift"));
                    shift.put("staffNo", item.getString("field_staff_no"));
                    shift.put("staffName", item.getString("field_staff_name"));
                    shift.put("roomName", item.getString("field_room_name"));
                    shiftList.add(shift);
                }
            }
        }

        return shiftList;
    }

    /**
     * 发送钉钉消息通知
     *
     * @param userIds 接收人用户ID列表
     * @param msgType 消息类型（text、markdown）
     * @param content 消息内容
     * @return 发送结果
     */
    public boolean sendMessage(List<String> userIds, String msgType, String content) {
        try {
            String accessToken = getAccessToken();
            String url = baseUrl + "/v1.0/robot/batchMessages/send";

            JSONObject requestBody = new JSONObject();
            requestBody.put("msg_type", msgType);
            requestBody.put("msg", JSON.parseObject(content));
            requestBody.put("to_user_id_list", String.join(",", userIds));

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-acs-dingtalk-access-token", accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            return response.getStatusCode() == HttpStatus.OK;

        } catch (Exception e) {
            log.error("发送钉钉消息失败", e);
            return false;
        }
    }

    /**
     * 发送值班提醒消息
     *
     * @param userIds 接收人用户ID列表
     * @param staffName 值班人员姓名
     * @param scheduleDate 值班日期
     * @param shift 班次
     * @param roomName 机房名称
     * @return 发送结果
     */
    public boolean sendShiftReminder(List<String> userIds, String staffName,
                                     String scheduleDate, String shift, String roomName) {
        String shiftText = "DAY".equals(shift) ? "白班（08:00-17:00）" : "夜班（18:00-次日07:00）";

        String content = String.format(
                "{\"text\":{\"content\":\"值班提醒\\n\\n您好，%s，明天%s您需要值班。\\n值班地点：%s\\n班次：%s\\n请提前做好值班准备。\"}}",
                staffName, scheduleDate, roomName, shiftText
        );

        return sendMessage(userIds, "text", content);
    }

    /**
     * 发送值班交接提醒
     *
     * @param userIds 接收人用户ID列表
     * @param staffName 值班人员姓名
     * @return 发送结果
     */
    public boolean sendHandoverReminder(List<String> userIds, String staffName) {
        String content = String.format(
                "{\"text\":{\"content\":\"值班交接提醒\\n\\n您好，%s，距离交班还有30分钟，请及时填写交接记录。\"}}",
                staffName
        );

        return sendMessage(userIds, "text", content);
    }

    /**
     * 发送值班异常告警
     *
     * @param userIds 接收人用户ID列表（管理员）
     * @param staffName 值班人员姓名
     * @param reason 异常原因
     * @return 发送结果
     */
    public boolean sendShiftAbnormalAlert(List<String> userIds, String staffName, String reason) {
        String content = String.format(
                "{\"text\":{\"content\":\"值班异常告警\\n\\n值班人员：%s\\n异常原因：%s\\n请及时处理！\"}}",
                staffName, reason
        );

        return sendMessage(userIds, "text", content);
    }

    /**
     * 发送临时权限审批结果通知
     *
     * @param userIds 接收人用户ID列表
     * @param applicantName 申请人姓名
     * @param approvalStatus 审批状态（1-通过 2-拒绝）
     * @param comment 审批意见
     * @return 发送结果
     */
    public boolean sendTempAccessApproval(List<String> userIds, String applicantName,
                                          Integer approvalStatus, String comment) {
        String statusText = approvalStatus == 1 ? "通过" : "拒绝";
        String content = String.format(
                "{\"text\":{\"content\":\"临时门禁权限审批结果\\n\\n申请人：%s\\n审批结果：%s\\n审批意见：%s\"}}",
                applicantName, statusText, comment
        );

        return sendMessage(userIds, "text", content);
    }
}
