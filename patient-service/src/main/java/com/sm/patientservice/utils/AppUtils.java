package com.sm.patientservice.utils;

import java.time.ZoneId;

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

}
