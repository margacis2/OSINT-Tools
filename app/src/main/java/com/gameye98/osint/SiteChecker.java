package com.gameye98.osint;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.ConnectException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SiteChecker {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";
    public static final String ACCEPT_HEADER = "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8";
    public static final String ACCEPT_LANG = "en-US,en;q=0.9";

    private static final Pattern RE_OS_URL = Pattern.compile("https?://(www\\.)?(github|gitlab|codeberg|bitbucket)\\.", Pattern.CASE_INSENSITIVE);
    private static final Pattern RE_OS_BODY = Pattern.compile("open[- ]source|apache[ -]2\\.0|mit license|\\bgpl\\b|gnu general public|source code", Pattern.CASE_INSENSITIVE);
    private static final Pattern RE_FREEMIUM_BODY = Pattern.compile("\\bpricing\\b|\\bplans\\b|\\bsubscription\\b|per month|/mo\\b|free trial|buy now|upgrade|premium plan|\\benterprise plan\\b", Pattern.CASE_INSENSITIVE);

    private static SSLContext lenientSslContext;

    static {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };
            lenientSslContext = SSLContext.getInstance("TLS");
            lenientSslContext.init(null, trustAllCerts, new SecureRandom());
        } catch (Exception e) {
            lenientSslContext = null;
        }
    }

    public static PricingType classifyPricing(String url, String body) {
        if (url != null && RE_OS_URL.matcher(url).find()) {
            return PricingType.OPEN_SOURCE;
        }
        if (body == null || body.trim().isEmpty()) {
            return PricingType.UNKNOWN;
        }
        if (RE_OS_BODY.matcher(body).find()) {
            return PricingType.OPEN_SOURCE;
        }
        if (RE_FREEMIUM_BODY.matcher(body).find()) {
            return PricingType.FREEMIUM;
        }
        return PricingType.FREE;
    }

    public static SiteCheckResult checkUrl(String targetUrl, int timeoutMs) {
        return checkUrl(targetUrl, timeoutMs, timeoutMs);
    }

    public static SiteCheckResult checkUrl(String targetUrl, int connectTimeoutMs, int readTimeoutMs) {
        if (targetUrl == null || targetUrl.trim().isEmpty()) {
            return new SiteCheckResult("", SiteStatus.DEAD, 0, "", PricingType.UNKNOWN, System.currentTimeMillis());
        }

        String currentUrl = targetUrl.trim();
        if (!currentUrl.startsWith("http://") && !currentUrl.startsWith("https://")) {
            currentUrl = "https://" + currentUrl;
        }

        int redirectCount = 0;
        final int maxRedirects = 4;
        int lastCode = 0;
        String finalUrl = currentUrl;
        String body = "";

        while (redirectCount < maxRedirects) {
            HttpURLConnection conn = null;
            try {
                URL urlObj = new URL(currentUrl);
                conn = (HttpURLConnection) urlObj.openConnection();

                if (conn instanceof HttpsURLConnection && lenientSslContext != null) {
                    HttpsURLConnection httpsConn = (HttpsURLConnection) conn;
                    httpsConn.setSSLSocketFactory(lenientSslContext.getSocketFactory());
                    httpsConn.setHostnameVerifier((hostname, session) -> true);
                }

                conn.setRequestMethod("GET");
                conn.setConnectTimeout(connectTimeoutMs);
                conn.setReadTimeout(readTimeoutMs);
                conn.setInstanceFollowRedirects(false); // Handle redirects manually for http->https support

                conn.setRequestProperty("User-Agent", USER_AGENT);
                conn.setRequestProperty("Accept", ACCEPT_HEADER);
                conn.setRequestProperty("Accept-Language", ACCEPT_LANG);
                conn.setRequestProperty("Sec-Ch-Ua", "\"Chromium\";v=\"124\", \"Google Chrome\";v=\"124\", \"Not-A.Brand\";v=\"99\"");
                conn.setRequestProperty("Sec-Ch-Ua-Mobile", "?0");
                conn.setRequestProperty("Sec-Ch-Ua-Platform", "\"Windows\"");

                conn.connect();
                lastCode = conn.getResponseCode();
                finalUrl = conn.getURL() != null ? conn.getURL().toString() : currentUrl;

                // Handle Redirects
                if (lastCode >= 300 && lastCode < 400) {
                    String location = conn.getHeaderField("Location");
                    if (location != null && !location.isEmpty()) {
                        if (location.startsWith("/")) {
                            URL base = new URL(currentUrl);
                            location = base.getProtocol() + "://" + base.getHost() + location;
                        }
                        currentUrl = location;
                        redirectCount++;
                        continue;
                    }
                }

                // Read body (read up to 20KB for fast detection)
                InputStream is = null;
                try {
                    if (lastCode >= 200 && lastCode < 400) {
                        is = conn.getInputStream();
                        body = readStream(is, 20000);
                    } else {
                        is = conn.getErrorStream();
                        if (is != null) {
                            body = readStream(is, 10000);
                        }
                    }
                } catch (Exception ignored) {
                } finally {
                    if (is != null) {
                        try { is.close(); } catch (Exception ignored) {}
                    }
                }

                break; // Completed request

            } catch (SocketTimeoutException e) {
                return new SiteCheckResult(targetUrl, SiteStatus.TIMEOUT, 0, finalUrl, PricingType.UNKNOWN, System.currentTimeMillis());
            } catch (UnknownHostException | ConnectException e) {
                return new SiteCheckResult(targetUrl, SiteStatus.DEAD, 0, finalUrl, PricingType.UNKNOWN, System.currentTimeMillis());
            } catch (Exception e) {
                // If SSL failure or other URL error, report DEAD or TIMEOUT
                String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                if (msg.contains("timeout") || msg.contains("timed out")) {
                    return new SiteCheckResult(targetUrl, SiteStatus.TIMEOUT, 0, finalUrl, PricingType.UNKNOWN, System.currentTimeMillis());
                }
                return new SiteCheckResult(targetUrl, SiteStatus.DEAD, 0, finalUrl, PricingType.UNKNOWN, System.currentTimeMillis());
            } finally {
                if (conn != null) {
                    try { conn.disconnect(); } catch (Exception ignored) {}
                }
            }
        }

        SiteStatus status;
        PricingType pricing;

        if (lastCode >= 200 && lastCode < 400) {
            status = SiteStatus.WORKING;
            pricing = classifyPricing(finalUrl, body);
        } else if (lastCode == 401 || lastCode == 403 || lastCode == 429 || lastCode == 503) {
            // Protected by Cloudflare, anti-bot challenge, or login wall (host is alive)
            status = SiteStatus.BLOCKED_OR_LOGIN;
            pricing = PricingType.UNKNOWN;
        } else if (lastCode == 404 || lastCode == 410) {
            status = SiteStatus.DEAD;
            pricing = PricingType.UNKNOWN;
        } else {
            status = SiteStatus.ERROR;
            pricing = PricingType.UNKNOWN;
        }

        return new SiteCheckResult(targetUrl, status, lastCode, finalUrl, pricing, System.currentTimeMillis());
    }

    private static String readStream(InputStream is, int maxBytes) {
        if (is == null) return "";
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int totalRead = 0;
            int nRead;
            while ((nRead = is.read(data, 0, Math.min(data.length, maxBytes - totalRead))) != -1) {
                buffer.write(data, 0, nRead);
                totalRead += nRead;
                if (totalRead >= maxBytes) break;
            }
            return buffer.toString("UTF-8").toLowerCase();
        } catch (Exception e) {
            return "";
        }
    }
}
