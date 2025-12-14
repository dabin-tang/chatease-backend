package com.dbt.chatease.Utils;

import org.springframework.stereotype.Component;

@Component
public class GroupIdGenerator {
    private static final String PREFIX = "GID";
    private final Snowflake snowflake;
    
    public GroupIdGenerator() {
        this.snowflake = new Snowflake(1, 1);
    }
    
    public String generate() {
        long id = snowflake.nextId();
        String numericPart = String.format("%09d", id % 1000000000L);
        return PREFIX + numericPart;
    }

    private static class Snowflake {
        private final long workerId;
        private final long datacenterId;
        private long sequence = 0L;
        private long lastTimestamp = -1L;
        
        public Snowflake(long workerId, long datacenterId) {
            this.workerId = workerId;
            this.datacenterId = datacenterId;
        }
        
        public synchronized long nextId() {
            long timestamp = System.currentTimeMillis();
            
            if (timestamp < lastTimestamp) {
                throw new RuntimeException("时钟回拨异常");
            }
            
            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & 0xFFF;
                if (sequence == 0) {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }
            
            lastTimestamp = timestamp;
            
            return ((timestamp - 1609459200000L) << 22) |
                   (datacenterId << 17) |
                   (workerId << 12) |
                   sequence;
        }
        
        private long tilNextMillis(long lastTimestamp) {
            long timestamp = System.currentTimeMillis();
            while (timestamp <= lastTimestamp) {
                timestamp = System.currentTimeMillis();
            }
            return timestamp;
        }
    }
}