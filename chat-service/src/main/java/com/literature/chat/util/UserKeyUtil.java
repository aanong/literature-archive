package com.literature.chat.util;

public final class UserKeyUtil {

    private UserKeyUtil() {
    }

    public static String build(Long userId, String userType) {
        String safeType = (userType == null || userType.isBlank()) ? "UNKNOWN" : userType.toUpperCase();
        return safeType + ":" + userId;
    }
}
