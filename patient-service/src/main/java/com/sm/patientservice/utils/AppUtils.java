package com.sm.patientservice.utils;

import java.time.Instant;
import java.time.ZoneId;

import com.google.protobuf.Timestamp;

public class AppUtils {
    
    private AppUtils() {
        // Private constructor to prevent instantiation
    }

    public static ZoneId getDefaultZoneSupplier() {
        return ZoneId.of("America/New_York"); // Default time zone
    }

    public static String getCurrentTimestamp() {
        return java.time.OffsetDateTime.now(
            getDefaultZoneSupplier()).toString();
    }

    public static String convertLocalDateTimeToString(java.time.LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }

        return localDateTime.atZone(getDefaultZoneSupplier()).toOffsetDateTime().toString();
    }

    public static Timestamp toProtoTimestamp(java.time.LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return Timestamp.newBuilder()
                .setSeconds(Instant.now().getEpochSecond())
                .setNanos(Instant.now().getNano())
                .build();
        }
        
        return Timestamp.newBuilder()
                .setSeconds(localDateTime.atZone(getDefaultZoneSupplier()).toEpochSecond())
                .setNanos(localDateTime.getNano())
                .build();
    }
    
    public static java.time.LocalDateTime fromProtoTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        
        return java.time.LocalDateTime.ofEpochSecond(
                timestamp.getSeconds(), 
                timestamp.getNanos(), 
                getDefaultZoneSupplier().getRules().getOffset(java.time.Instant.now()));
    }

}
