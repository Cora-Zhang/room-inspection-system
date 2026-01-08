package com.roominspection.backend.accesscontrol;

/**
 * 门禁事件监听器接口
 */
public interface DoorAccessEventListener {

    /**
     * 门禁事件类型
     */
    enum EventType {
        DOOR_OPEN,           // 开门
        DOOR_CLOSE,          // 关门
        ACCESS_DENIED,       // 拒绝访问
        ACCESS_GRANTED,      // 允许访问
        ALARM,               // 告警
        DOOR_OPEN_TOO_LONG,  // 门开启时间过长
        FORCED_OPEN,         // 强行开门
        INVALID_CARD,        // 无效卡
        USER_AUTHENTICATION  // 用户认证
    }

    /**
     * 门禁事件
     */
    class DoorAccessEvent {
        private EventType eventType;
        private String doorId;
        private String userId;
        private String userName;
        private long timestamp;
        private Map<String, Object> data;

        public DoorAccessEvent(EventType eventType, String doorId, String userId,
                               String userName, long timestamp, Map<String, Object> data) {
            this.eventType = eventType;
            this.doorId = doorId;
            this.userId = userId;
            this.userName = userName;
            this.timestamp = timestamp;
            this.data = data;
        }

        public EventType getEventType() {
            return eventType;
        }

        public String getDoorId() {
            return doorId;
        }

        public String getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public Map<String, Object> getData() {
            return data;
        }
    }

    /**
     * 处理门禁事件
     */
    void onDoorAccessEvent(DoorAccessEvent event);
}
