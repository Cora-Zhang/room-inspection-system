package com.roominspection.backend.protocol;

import lombok.extern.slf4j.Slf4j;
import org.snmp4j.*;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;

import java.util.*;

/**
 * APC设备协议适配器
 * 支持APC NetBotz环境监控系统、APC UPS等设备
 * 使用SNMP协议进行通信
 */
@Slf4j
public class APCProtocolAdapter implements DeviceProtocol {

    private static final String PROTOCOL_NAME = "APC SNMP";
    private static final String PROTOCOL_VERSION = "1.0";

    private Snmp snmp;
    private TransportMapping<UdpAddress> transport;
    private Target target;
    private int timeout = 5000; // 默认超时5秒
    private String lastError;

    // APC SNMP OID定义
    private static final String APC_SYS_DESCR = "1.3.6.1.2.1.1.1.0"; // 系统描述
    private static final String APC_SYS_UPTIME = "1.3.6.1.2.1.1.3.0"; // 系统运行时间
    private static final String APC_CONTACT = "1.3.6.1.2.1.1.4.0"; // 联系人信息
    private static final String APC_NAME = "1.3.6.1.2.1.1.5.0"; // 设备名称

    // APC环境监控OID
    private static final String APC_TEMP_SENSOR_PREFIX = "1.3.6.1.4.1.318.1.1.10.2.3.2.1"; // 温度传感器
    private static final String APC_HUMIDITY_SENSOR_PREFIX = "1.3.6.1.4.1.318.1.1.10.2.3.3.1"; // 湿度传感器
    private static final String APC_TEMP_ALARM_PREFIX = "1.3.6.1.4.1.318.1.1.10.2.3.4.1"; // 温度告警
    private static final String APC_HUMIDITY_ALARM_PREFIX = "1.3.6.1.4.1.318.1.1.10.2.3.5.1"; // 湿度告警

    // APC UPS OID
    private static final String APC_UPS_OUTPUT_LOAD_PREFIX = "1.3.6.1.4.1.318.1.1.1.4.3.3"; // UPS输出负载
    private static final String APC_UPS_BATTERY_CAPACITY_PREFIX = "1.3.6.1.4.1.318.1.1.1.2.2.1"; // 电池容量
    private static final String APC_UPS_OUTPUT_VOLTAGE_PREFIX = "1.3.6.1.4.1.318.1.1.1.4.2.1"; // 输出电压

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

            log.info("APC设备连接成功: {}:{}", host, port);
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("APC设备连接失败", e);
            throw new Exception("APC设备连接失败: " + e.getMessage());
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
            log.info("APC设备已断开连接");
        } catch (Exception e) {
            log.error("断开APC设备连接失败", e);
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
            log.error("读取APC设备数据失败", e);
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
                log.info("写入APC设备数据成功: {} = {}", address, value);
            } else {
                throw new Exception("写入数据失败：无响应");
            }
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("写入APC设备数据失败", e);
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
            pdu.add(new VariableBinding(new OID(APC_SYS_DESCR)));
            pdu.add(new VariableBinding(new OID(APC_SYS_UPTIME)));
            pdu.add(new VariableBinding(new OID(APC_NAME)));
            pdu.add(new VariableBinding(new OID(APC_CONTACT)));
            pdu.setType(PDU.GET);

            ResponseEvent responseEvent = snmp.send(pdu, target);
            if (responseEvent != null && responseEvent.getResponse() != null) {
                PDU response = responseEvent.getResponse();
                for (VariableBinding vb : response.getVariableBindings()) {
                    String oid = vb.getOid().toString();
                    String value = vb.getVariable().toString();

                    if (oid.equals(APC_SYS_DESCR)) {
                        status.put("description", value);
                    } else if (oid.equals(APC_SYS_UPTIME)) {
                        status.put("uptime", value);
                    } else if (oid.equals(APC_NAME)) {
                        status.put("name", value);
                    } else if (oid.equals(APC_CONTACT)) {
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
            log.error("读取APC设备状态失败", e);
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
            log.error("批量读取APC设备数据失败", e);
            throw e;
        }
    }

    @Override
    public void setAlarmThreshold(String address, String alarmType, double threshold) throws Exception {
        // APC设备的告警阈值设置通过SNMP SET实现
        // 具体的OID需要根据设备型号和告警类型确定
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
     * 读取APC温度传感器数据
     */
    public Map<String, Object> readTemperatureSensors() throws Exception {
        Map<String, Object> temperatures = new HashMap<>();
        Map<String, String> addressMap = new HashMap<>();

        // 读取多个温度传感器
        for (int i = 1; i <= 10; i++) {
            String oid = APC_TEMP_SENSOR_PREFIX + i + ".6.0";
            addressMap.put(oid, "temperature");
        }

        Map<String, Object> result = batchRead(addressMap);
        temperatures.putAll(result);
        temperatures.put("sensorType", "APC Temperature");
        return temperatures;
    }

    /**
     * 读取APC湿度传感器数据
     */
    public Map<String, Object> readHumiditySensors() throws Exception {
        Map<String, Object> humidity = new HashMap<>();
        Map<String, String> addressMap = new HashMap<>();

        // 读取多个湿度传感器
        for (int i = 1; i <= 10; i++) {
            String oid = APC_HUMIDITY_SENSOR_PREFIX + i + ".6.0";
            addressMap.put(oid, "humidity");
        }

        Map<String, Object> result = batchRead(addressMap);
        humidity.putAll(result);
        humidity.put("sensorType", "APC Humidity");
        return humidity;
    }

    /**
     * 读取APC UPS状态
     */
    public Map<String, Object> readUPSStatus() throws Exception {
        Map<String, Object> upsStatus = new HashMap<>();
        Map<String, String> addressMap = new HashMap<>();

        // 读取UPS输出负载
        for (int i = 1; i <= 3; i++) {
            String oid = APC_UPS_OUTPUT_LOAD_PREFIX + i + ".1.0";
            addressMap.put(oid, "load");
        }

        // 读取电池容量
        String oid = APC_UPS_BATTERY_CAPACITY_PREFIX + "1.0";
        addressMap.put(oid, "batteryCapacity");

        Map<String, Object> result = batchRead(addressMap);
        upsStatus.putAll(result);
        upsStatus.put("deviceType", "APC UPS");
        return upsStatus;
    }
}
