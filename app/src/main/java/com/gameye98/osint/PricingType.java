package com.gameye98.osint;

public enum PricingType {
    UNKNOWN("Unknown", "#94A3B8"),
    FREE("Free", "#10B981"),
    FREEMIUM("Freemium", "#8B5CF6"),
    OPEN_SOURCE("Open Source", "#06B6D4"),
    PAID("Paid", "#F59E0B");

    private final String displayName;
    private final String colorHex;

    PricingType(String displayName, String colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public static PricingType fromString(String pricingStr) {
        if (pricingStr == null) return UNKNOWN;
        String s = pricingStr.trim().toLowerCase();
        switch (s) {
            case "free":
                return FREE;
            case "freemium":
                return FREEMIUM;
            case "open_source":
            case "opensource":
            case "open source":
                return OPEN_SOURCE;
            case "paid":
                return PAID;
            default:
                return UNKNOWN;
        }
    }
}
