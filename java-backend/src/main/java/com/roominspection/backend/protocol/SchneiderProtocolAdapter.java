package com.roominspection.backend.protocol;

import com.modbus4j.ModbusFactory;
import com.modbus4j.ModbusMaster;
import com.modbus4j.msg.ReadHoldingRegistersRequest;
import com.modbus4j.msg.ReadHoldingRegistersResponse;
import com.modbus4j.msg.WriteRegisterRequest;
import com.modbus4j.msg.WriteRegisterResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 施耐德设备协议适配器
 * 支持施耐德PDU、APC Modbus设备等
 * 使用Modbus TCP协议进行通信
 */
@Slf4j
public class SchneiderProtocolAdapter implements DeviceProtocol {

    private static final String PROTOCOL_NAME = "Schneider Modbus TCP";
    private static final String PROTOCOL_VERSION = "1.0";

    private ModbusMaster modbusMaster;
    private String host;
    private int port;
    private int timeout = 5000; // 默认超时5秒
    private String lastError;
    private int slaveId = 1; // 默认从站ID

    // 施耐德Modbus寄存器地址定义
    private static final int MODBUS_VOLTAGE_ADDR = 100; // 电压
    private static final int MODBUS_CURRENT_ADDR = 102; // 电流
    private static final int MODBUS_POWER_ADDR = 104; // 功率
    private static final int MODBUS_FREQUENCY_ADDR = 106; // 频率
    private static final int MODBUS_TEMP_ADDR = 200; // 温度
    private static final int MODBUS_HUMIDITY_ADDR = 202; // 湿度
    private static final int MODBUS_ALARM_STATUS_ADDR = 300; // 告警状态

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
            this.host = host;
            this.port = port;

            // 获取从站ID
            if (params.containsKey("slaveId")) {
                this.slaveId = (int) params.get("slaveId");
            }

            // 创建Modbus TCP主站
            ModbusFactory factory = new ModbusFactory();
            modbusMaster = factory.createTcpMaster(
                    new com.modbus4j.ip.tcp.TcpParameters(
                            java.net.InetAddress.getByName(host), port, false
                    ),
                    true
            );

            modbusMaster.setRetries(3);
            modbusMaster.setTimeout(timeout);
            modbusMaster.setConnected(true);

            log.info("施耐德设备连接成功: {}:{}", host, port);
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("施耐德设备连接失败", e);
            throw new Exception("施耐德设备连接失败: " + e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        try {
            if (modbusMaster != null) {
                modbusMaster.destroy();
            }
            log.info("施耐德设备已断开连接");
        } catch (Exception e) {
            log.error("断开施耐德设备连接失败", e);
        }
    }

    @Override
    public boolean isConnected() {
        return modbusMaster != null && modbusMaster.isInitialized();
    }

    @Override
    public Map<String, Object> readData(String address, String dataType) throws Exception {
        if (!isConnected()) {
            throw new Exception("设备未连接");
        }

        try {
            int addr = Integer.parseInt(address);
            ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(
                    slaveId, addr, 1
            );
            ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse)
                    modbusMaster.send(request);

            if (response.isException()) {
                throw new Exception("Modbus异常: " + response.getExceptionMessage());
            }

            short value = response.getShortValue(0);
            Map<String, Object> result = new HashMap<>();
            result.put("address", address);
            result.put("dataType", dataType);
            result.put("value", value);
            result.put("timestamp", System.currentTimeMillis());

            return result;
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("读取施耐德设备数据失败", e);
            throw e;
        }
    }

    @Override
    public void writeData(String address, String dataType, Object value) throws Exception {
        if (!isConnected()) {
            throw new Exception("设备未连接");
        }

        try {
            int addr = Integer.parseInt(address);
            int intValue = Integer.parseInt(value.toString());

            WriteRegisterRequest request = new WriteRegisterRequest(
                    slaveId, addr, intValue
            );
            WriteRegisterResponse response = (WriteRegisterResponse)
                    modbusMaster.send(request);

            if (response.isException()) {
                throw new Exception("Modbus异常: " + response.getExceptionMessage());
            }

            log.info("写入施耐德设备数据成功: {} = {}", address, value);
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("写入施耐德设备数据失败", e);
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

            // 读取电压
            Map<String, Object> voltage = readData(String.valueOf(MODBUS_VOLTAGE_ADDR), "voltage");
            status.put("voltage", voltage.get("value"));

            // 读取电流
            Map<String, Object> current = readData(String.valueOf(MODBUS_CURRENT_ADDR), "current");
            status.put("current", current.get("value"));

            // 读取功率
            Map<String, Object> power = readData(String.valueOf(MODBUS_POWER_ADDR), "power");
            status.put("power", power.get("value"));

            // 读取告警状态
            Map<String, Object> alarm = readData(String.valueOf(MODBUS_ALARM_STATUS_ADDR), "alarm");
            status.put("alarmStatus", alarm.get("value"));

            status.put("timestamp", System.currentTimeMillis());
            status.put("status", "ONLINE");
            status.put("manufacturer", "Schneider");

            return status;
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("读取施耐德设备状态失败", e);
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

            // 批量读取多个寄存器
            for (Map.Entry<String, String> entry : addressMap.entrySet()) {
                try {
                    Map<String, Object> data = readData(entry.getKey(), entry.getValue());
                    result.put(entry.getKey(), data.get("value"));
                } catch (Exception e) {
                    log.warn("读取寄存器失败: {}", entry.getKey());
                    result.put(entry.getKey(), null);
                }
            }

            result.put("timestamp", System.currentTimeMillis());
            return result;
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("批量读取施耐德设备数据失败", e);
            throw e;
        }
    }

    @Override
    public void setAlarmThreshold(String address, String alarmType, double threshold) throws Exception {
        // 施耐德设备的告警阈值设置通过Modbus SET实现
        int intValue = (int) threshold;
        writeData(address, "threshold", intValue);
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
        if (modbusMaster != null) {
            modbusMaster.setTimeout(timeout);
        }
    }

    /**
     * 读取施耐德PDU电压数据
     */
    public Map<String, Object> readPDUVoltage() throws Exception {
        Map<String, Object> voltageData = new HashMap<>();

        // 读取三相电压
        for (int phase = 0; phase < 3; phase++) {
            int addr = MODBUS_VOLTAGE_ADDR + phase * 2;
            Map<String, Object> data = readData(String.valueOf(addr), "voltage");
            voltageData.put("phase" + (phase + 1), data.get("value"));
        }

        voltageData.put("deviceType", "Schneider PDU");
        return voltageData;
    }

    /**
     * 读取施耐德PDU功率数据
     */
    public Map<String, Object> readPDUPower() throws Exception {
        Map<String, Object> powerData = new HashMap<>();

        // 读取三相功率
        for (int phase = 0; phase < 3; phase++) {
            int addr = MODBUS_POWER_ADDR + phase * 2;
            Map<String, Object> data = readData(String.valueOf(addr), "power");
            powerData.put("phase" + (phase + 1), data.get("value"));
        }

        powerData.put("deviceType", "Schneider PDU");
        return powerData;
    }

    /**
     * 读取施耐德环境监控数据
     */
    public Map<String, Object> readEnvironmentMonitor() throws Exception {
        Map<String, Object> envData = new HashMap<>();

        // 读取温度
        Map<String, Object> temp = readData(String.valueOf(MODBUS_TEMP_ADDR), "temperature");
        envData.put("temperature", temp.get("value"));

        // 读取湿度
        Map<String, Object> humidity = readData(String.valueOf(MODBUS_HUMIDITY_ADDR), "humidity");
        envData.put("humidity", humidity.get("value"));

        envData.put("deviceType", "Schneider Environment Monitor");
        return envData;
    }
}
