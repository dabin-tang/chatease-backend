package com.dbt.chatease.Utils;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * User ID Generator that creates unique numeric IDs (12 digits).
 * Uses a modified Snowflake pattern: 8-digit timestamp + 4-digit sequence.
 */
@Component
public class UserIdGenerator {

    private static final int ID_LENGTH = 12;
    private static final int MAX_SEQUENCE = 1000;

    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    private volatile long lastTimestamp = -1L;

    /**
     * Generates a 12-digit ID starting with "UID".
     * Structure: UID + 6-digit Timestamp + 3-digit Sequence.
     */
    public synchronized String generateUniqueId() {
        long currentTimestamp = System.currentTimeMillis();

        // 获取6位时间戳（秒级精度，取模1000000）
        long timestampPart = (currentTimestamp / 1000) % 1000000L;

        if (currentTimestamp == lastTimestamp) {
            long seq = SEQUENCE.incrementAndGet();
            if (seq >= MAX_SEQUENCE) {
                currentTimestamp = tilNextMillis(lastTimestamp);
                SEQUENCE.set(0);
            }
        } else {
            SEQUENCE.set(0);
        }

        lastTimestamp = currentTimestamp;
        long sequencePart = SEQUENCE.get();

        //Formatted as: UID + 6-digit timestamp + 3-digit serial number
        String id = String.format("UID%06d%03d", timestampPart, sequencePart);

        return id;
    }


    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}