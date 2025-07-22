package com.ruoyi.userTools;

public class SnowflakeIdGenerator {

    // 起始时间戳（可根据业务自定义，单位：毫秒）
    private final static long START_TIMESTAMP = 1609459200000L; // 2021-01-01

    // 每一部分所占位数
    private final static long DATA_CENTER_BITS = 5L;
    private final static long MACHINE_BITS = 5L;
    private final static long SEQUENCE_BITS = 12L;

    // 最大值
    private final static long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_BITS); // 31
    private final static long MAX_MACHINE_ID = ~(-1L << MACHINE_BITS);         // 31
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);          // 4095

    // 每一部分左移位数
    private final static long MACHINE_SHIFT = SEQUENCE_BITS;
    private final static long DATA_CENTER_SHIFT = SEQUENCE_BITS + MACHINE_BITS;
    private final static long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_BITS + DATA_CENTER_BITS;

    private final long dataCenterId;
    private final long machineId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(long dataCenterId, long machineId) {
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException("dataCenterId 取值范围：0~" + MAX_DATA_CENTER_ID);
        }
        if (machineId > MAX_MACHINE_ID || machineId < 0) {
            throw new IllegalArgumentException("machineId 取值范围：0~" + MAX_MACHINE_ID);
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public synchronized long nextId() {
        long currentTimestamp = currentTimeMillis();

        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("系统时间回拨，拒绝生成 ID");
        }

        if (currentTimestamp == lastTimestamp) {
            // 同一毫秒内，序列递增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                // 当前毫秒内序列耗尽，等待下一毫秒
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            // 不同毫秒，序列重置
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_SHIFT)
                | (machineId << MACHINE_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp <= lastTimestamp) {
            currentTimestamp = currentTimeMillis();
        }
        return currentTimestamp;
    }

    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}