package com.roominspection.backend.protocol;

import lombok.extern.slf4j.Slf4j;
import org.snmp4j.*;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;

import java.util.*;

/**
 * 艾默生设备协议适配器
 * 支持艾默生Liebert UPS、Liebert PDU、NetSure电源等设备
 * 使用SNMP协议进行通信
 */
@Slf4j
public class EmersonProtocolAdapter implements DeviceProtocol {

    private static final String PROTOCOL_NAME = "Emerson SNMP";
    private static final String PROTOCOL_VERSION = "1.0";

    private Snmp snmp;
    private TransportMapping<UdpAddress> transport;
    private Target target;
    private int timeout = 5000; // 默认超时5秒
    private String lastError;

    // 艾默生SNMP OID定义
    private static final String EMERSON_SYS_DESCR = "1.3.6.1.2.1.1.1.0"; // 系统描述
    private static final String EMERSON_SYS_UPTIME = "1.3.6.1.2.1.1.3.0"; // 系统运行时间
    private static final String EMERSON_CONTACT = "1.3.6.1.2.1.1.4.0"; // 联系人信息
    private static final String EMERSON_NAME = "1.3.6.1.2.1.1.5.0"; // 设备名称

    // 艾默生UPS OID
    private static final String EMERSON_UPS_OUTPUT_VOLTAGE_PREFIX = "1.3.6.1.4.1.476.1.42.3.5.10.1.1"; // 输出电压
    private static final String EMERSON_UPS_OUTPUT_CURRENT_PREFIX = "1.3.6.1.4.1.476.1.42.3.5.10.1.2"; // 输出电流
    private static final String EMERSON_UPS_OUTPUT_LOAD_PREFIX = "1.3.6.1.4.1.476.1.42.3.5.10.1.4"; // 输出负载
    private static final String EMERSON_UPS_BATTERY_STATUS_PREFIX = "1.3.6.1.4.1.476.1.42.3.5.10.2.1"; // 电池状态
    private static final String EMERSON_UPS_BATTERY_RUNTIME = "1.3.6.1.4.1.476.1.42.3.5.10.2.1.5.0"; // 剩余运行时间

    // 艾默生PDU OID
    private static final String EMERSON_PDU_VOLTAGE_PREFIX = "1.3.6.1.4.1.476.1.42.3.6.10.1.1"; // PDU电压
    private static final String EMERSON_PDU_CURRENT_PREFIX = "1.3.6.1.4.1.476.1.42.3.6.10.1.2"; // PDU电流
    private static final String EMERSON_PDU_POWER_PREFIX = "1.3.6.1.4.1.476.1.42.3.6.10.1.3"; // PDU功率

    // 艾默生环境监控OID
    private static final String EMERSON_TEMP_SENSOR_PREFIX = "1.3.6.1.4.1.476.1.42.3.8.10.1.1"; // 温度传感器
    private static final String EMERSON_HUMIDITY_SENSOR_PREFIX = "1.3.6.1.4.1.476.1.42.3.8.10.1.2"; // 湿度传感器

    @Override
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    @Override
    public String getProtocolVersion() {
        return PROTOCOL_VERSION;
    }

    @Override
    public void connect(String host, int port, Map<String, Object> params) throws Exception {
        try {
            // 创建SNMP传输映射
            transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();

            // 创建SNMP目标
            String community = params.get("community") != null ?
                    params.get("community").toString() : "public";
            target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(new UdpAddress(host + "/" + port));
            target.setRetries(3);
            target.setTimeout(timeout);
            target.setVersion(SnmpConstants.version2c);

            log.info("艾默生设备连接成功: {}:{}", host, port);
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("艾默生设备连接失败", e);
            throw new Exception("艾默生设备连接失败: " + e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        try {
            if (snmp != null) {
                snmp.close();
            }
            if (transport != null) {
                transport.close();
            }
            log.info("艾默生设备已断开连接");
        } catch (Exception e) {
            log.error("断开艾默生设备连接失败", e);
        }
    }

    @Override
    public boolean isConnected() {
        return snmp != null && transport != null && transport.isListening();
    }

    @Override
    public Map<String, Object> readData(String address, String dataType) throws Exception {
        if (!isConnected()) {
            throw new Exception("设备未连接");
        }

        try {
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(address)));
            pdu.setType(PDU.GET);

            ResponseEvent responseEvent = snmp.send(pdu, target);
            if (responseEvent != null && responseEvent.getResponse() != null) {
                PDU response = responseEvent.getResponse();
                VariableBinding vb = response.get(0);
                Map<String, Object> result = new HashMap<>();
                result.put("address", address);
                result.put("dataType", dataType);
                result.put("value", vb.getVariable().toString());
                result.put("timestamp", System.currentTimeMillis());
                return result;
            } else {
                throw new Exception("读取数据失败：无响应");
            }
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("读取艾默生设备数据失败", e);
            throw e;
        }
    }

    @Override
    public void writeData(String address, String dataType, Object value) throws Exception {
        if (!isConnected()) {
            throw new Exception("设备未连接");
        }

        try {
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(address), new OctetString(value.toString())));
            pdu.setType(PDU.SET);

            ResponseEvent responseEvent = snmp.send(pdu, target);
            if (responseEvent != null && responseEvent.getResponse() != null) {
                log.info("写入艾默生设备数据成功: {} = {}", address, value);
            } else {
                throw new Exception("写入数据失败：无响应");
            }
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("写入艾默生设备数据失败", e);
            throw e;
        }
    }

    @Override
    public Map<String, Object> readDeviceStatus() throws Exception {
        if (!isConnected()) {
            throw new Exception("设备未连接");
        }

        try {
            Map<String, Object> status = new HashMap<>();

            // 读取系统描述
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(EMERSON_SYS_DESCR)));
            pdu.add(new VariableBinding(new OID(EMERSON_SYS_UPTIME)));
            pdu.add(new VariableBinding(new OID(EMERSON_NAME)));
            pdu.add(new VariableBinding(new OID(EMERSON_CONTACT)));
            pdu.setType(PDU.GET);

            ResponseEvent responseEvent = snmp.send(pdu, target);
            if (responseEvent != null && responseEvent.getResponse() != null) {
                PDU response = responseEvent.getResponse();
                for (VariableBinding vb : response.getVariableBindings()) {
                    String oid = vb.getOid().toString();
                    String value = vb.getVariable().toString();

                    if (oid.equals(EMERSON_SYS_DESCR)) {
                        status.put("description", value);
                    } else if (oid.equals(EMERSON_SYS_UPTIME)) {
                        status.put("uptime", value);
                    } else if (oid.equals(EMERSON_NAME)) {
                        status.put("name", value);
                    } else if (oid.equals(EMERSON_CONTACT)) {
                        status.put("contact", value);
                    }
                }

                status.put("timestamp", System.currentTimeMillis());
                status.put("status", "ONLINE");
            } else {
                status.put("status", "OFFLINE");
            }

            return status;
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("读取艾默生设备状态失败", e);
            throw e;
        }
    }

    @Override
    public Map<String, Object> batchRead(Map<String, String> addressMap) throws Exception {
        if (!isConnected()) {
            throw new Exception("设备未连接");
        }

        try {
            Map<String, Object> result = new HashMap<>();
            PDU pdu = new PDU();

            // 添加所有要读取的OID
            for (Map.Entry<String, String> entry : addressMap.entrySet()) {
                pdu.add(new VariableBinding(new OID(entry.getKey())));
            }
            pdu.setType(PDU.GET);

            ResponseEvent responseEvent = snmp.send(pdu, target);
            if (responseEvent != null && responseEvent.getResponse() != null) {
                PDU response = responseEvent.getResponse();
                for (VariableBinding vb : response.getVariableBindings()) {
                    String oid = vb.getOid().toString();
                    String dataType = addressMap.get(oid);
                    if (dataType != null) {
                        result.put(oid, vb.getVariable().toString());
                    }
                }
            }

            result.put("timestamp", System.currentTimeMillis());
            return result;
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("批量读取艾默生设备数据失败", e);
            throw e;
        }
    }

    @Override
    public void setAlarmThreshold(String address, String alarmType, double threshold) throws Exception {
        // 艾默生设备的告警阈值设置通过SNMP SET实现
        writeData(address, "threshold", threshold);
    }

    @Override
    public Map<String, Object> getDeviceInfo() throws Exception {
        return readDeviceStatus();
    }

    @Override
    public String getLastError() {
        return lastError;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(int timeout) {
        this.timeout = timeout;
        if (target != null) {
            target.setTimeout(timeout);
        }
    }

    /**
     * 读取艾默生UPS状态
     */
    public Map<String, Object> readUPSStatus() throws Exception {
        Map<String, Object> upsStatus = new HashMap<>();
        Map<String, String> addressMap = new HashMap<>();

        // 读取三相输出电压
        for (int i = 1; i <= 3; i++) {
            String oid = EMERSON_UPS_OUTPUT_VOLTAGE_PREFIX + i + ".0";
            addressMap.put(oid, "voltage");
        }

        // 读取三相输出电流
        for (int i = 1; i <= 3; i++) {
            String oid = EMERSON_UPS_OUTPUT_CURRENT_PREFIX + i + ".0";
            addressMap.put(oid, "current");
        }

        // 读取电池剩余运行时间
        addressMap.put(EMERSON_UPS_BATTERY_RUNTIME, "runtime");

        Map<String, Object> result = batchRead(addressMap);
        upsStatus.putAll(result);
        upsStatus.put("deviceType", "Emerson Liebert UPS");
        return upsStatus;
    }

    /**
     * 读取艾默生PDU状态
     */
    public Map<String, Object> readPDUStatus() throws Exception {
        Map<String, Object> pduStatus = new HashMap<>();
        Map<String, String> addressMap = new HashMap<>();

        // 读取多路电压
        for (int i = 1; i <= 24; i++) {
            String oid = EMERSON_PDU_VOLTAGE_PREFIX + i + ".0";
            addressMap.put(oid, "voltage");
        }

        // 读取多路电流
        for (int i = 1; i <= 24; i++) {
            String oid = EMERSON_PDU_CURRENT_PREFIX + i + ".0";
            addressMap.put(oid, "current");
        }

        Map<String, Object> result = batchRead(addressMap);
        pduStatus.putAll(result);
        pduStatus.put("deviceType", "Emerson PDU");
        return pduStatus;
    }

    /**
     * 读取艾默生环境监控数据
     */
    public Map<String, Object> readEnvironmentMonitor() throws Exception {
        Map<String, Object> envData = new HashMap<>();
        Map<String, String> addressMap = new HashMap<>();

        // 读取多个温度传感器
        for (int i = 1; i <= 8; i++) {
            String oid = EMERSON_TEMP_SENSOR_PREFIX + i + ".0";
            addressMap.put(oid, "temperature");
        }

        // 读取多个湿度传感器
        for (int i = 1; i <= 8; i++) {
            String oid = EMERSON_HUMIDITY_SENSOR_PREFIX + i + ".0";
            addressMap.put(oid, "humidity");
        }

        Map<String, Object> result = batchRead(addressMap);
        envData.putAll(result);
        envData.put("deviceType", "Emerson Environment Monitor");
        return envData;
    }
}
