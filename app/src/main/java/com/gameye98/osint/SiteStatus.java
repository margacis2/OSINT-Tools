package com.gameye98.osint;

import android.graphics.Color;

public enum SiteStatus {
    WORKING("Online", "#10B981", 1),          // Green (Working 2xx, 3xx)
    BLOCKED_OR_LOGIN("Protected", "#3B82F6", 2),// Blue (Cloudflare, 401, 403, 429, 503)
    PENDING("Pending", "#94A3B8", 3),          // Slate gray
    TIMEOUT("Timeout", "#F59E0B", 4),          // Amber
    ERROR("Error", "#EF4444", 5),              // Red
    DEAD("Offline", "#64748B", 6);             // Dark gray / Dead link

    private final String displayName;
    private final String colorHex;
    private final int sortPriority;

    SiteStatus(String displayName, String colorHex, int sortPriority) {
        this.displayName = displayName;
        this.colorHex = colorHex;
        this.sortPriority = sortPriority;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public int getColorInt() {
        return Color.parseColor(colorHex);
    }

    public int getSortPriority() {
        return sortPriority;
    }

    public static SiteStatus fromString(String statusStr) {
        if (statusStr == null) return PENDING;
        String s = statusStr.trim().toLowerCase();
        switch (s) {
            case "working":
            case "online":
                return WORKING;
            case "blocked_or_login":
            case "protected":
            case "login":
                return BLOCKED_OR_LOGIN;
            case "dead":
            case "offline":
                return DEAD;
            case "timeout":
                return TIMEOUT;
            case "error":
                return ERROR;
            default:
                return PENDING;
        }
    }
}
