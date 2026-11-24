package com.gameye98.osint;

import java.io.Serializable;

public class SiteCheckResult implements Serializable {
    private final String url;
    private final SiteStatus status;
    private final int httpCode;
    private final String finalUrl;
    private final PricingType pricing;
    private final long lastCheckedTimestamp;

    public SiteCheckResult(String url, SiteStatus status, int httpCode, String finalUrl, PricingType pricing, long lastCheckedTimestamp) {
        this.url = url != null ? url.trim() : "";
        this.status = status != null ? status : SiteStatus.PENDING;
        this.httpCode = httpCode;
        this.finalUrl = finalUrl != null ? finalUrl.trim() : "";
        this.pricing = pricing != null ? pricing : PricingType.UNKNOWN;
        this.lastCheckedTimestamp = lastCheckedTimestamp;
    }

    public static SiteCheckResult defaultPending(String url) {
        return new SiteCheckResult(url, SiteStatus.PENDING, 0, url, PricingType.UNKNOWN, 0);
    }

    public String getUrl() {
        return url;
    }

    public SiteStatus getStatus() {
        return status;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public String getFinalUrl() {
        return finalUrl;
    }

    public PricingType getPricing() {
        return pricing;
    }

    public long getLastCheckedTimestamp() {
        return lastCheckedTimestamp;
    }

    public boolean isWorking() {
        return status == SiteStatus.WORKING;
    }

    public boolean isDead() {
        return status == SiteStatus.DEAD;
    }

    @Override
    public String toString() {
        return "SiteCheckResult{" +
                "url='" + url + '\'' +
                ", status=" + status +
                ", httpCode=" + httpCode +
                ", pricing=" + pricing +
                ", lastCheckedTimestamp=" + lastCheckedTimestamp +
                '}';
    }
}
