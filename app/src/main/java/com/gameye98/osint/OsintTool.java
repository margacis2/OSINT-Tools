package com.gameye98.osint;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class OsintTool implements Serializable {
    private final String url;
    private final String title;
    private final String iconName;
    private final String domain;
    private final Set<String> categories;
    private final String primaryCategory;
    private final boolean recent;
    private final String addedDate;
    private final String toolType;
    private boolean favorite;

    public OsintTool(String url, String title, String iconName, boolean recent, String addedDate, String toolType, Set<String> customCategories) {
        this.url = url != null ? url.trim() : "";
        this.title = title != null ? title.trim() : "";
        this.iconName = iconName != null ? iconName.trim() : "default";
        this.domain = extractDomain(this.url);
        this.recent = recent;
        this.addedDate = addedDate != null ? addedDate.trim() : "";
        this.toolType = toolType != null ? toolType.trim() : "Web Service";
        this.favorite = false;

        Set<String> detected = new LinkedHashSet<>();
        if (customCategories != null && !customCategories.isEmpty()) {
            detected.addAll(customCategories);
        }
        detectCategories(this.url, this.title, detected);
        if (detected.isEmpty()) {
            detected.add("General Investigation Tools");
        }
        this.categories = Collections.unmodifiableSet(detected);
        this.primaryCategory = detected.iterator().next();
    }

    public OsintTool(String url, String title, String iconName) {
        this(url, title, iconName, false, "", "Web Service", null);
    }

    public static OsintTool parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split(Pattern.quote("|"));
        if (parts.length >= 3) {
            return new OsintTool(parts[0], parts[1], parts[2]);
        } else if (parts.length == 2) {
            return new OsintTool(parts[0], parts[1], "default");
        } else if (parts.length == 1) {
            return new OsintTool(parts[0], parts[0], "default");
        }
        return null;
    }

    public static OsintTool parseRecentLine(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split(Pattern.quote("|"));
        if (parts.length >= 6) {
            String url = parts[0];
            String title = parts[1];
            String icon = parts[2];
            String toolType = parts[3];
            String addedDate = parts[4];
            String[] cats = parts[5].split(",");
            Set<String> customCats = new LinkedHashSet<>();
            for (String c : cats) {
                if (!c.trim().isEmpty()) customCats.add(c.trim());
            }
            return new OsintTool(url, title, icon, true, addedDate, toolType, customCats);
        } else {
            return parseLine(line);
        }
    }

    private static String extractDomain(String rawUrl) {
        if (rawUrl == null || rawUrl.isEmpty()) return "";
        try {
            URI uri = new URI(rawUrl);
            String host = uri.getHost();
            if (host != null) {
                if (host.startsWith("www.")) {
                    return host.substring(4);
                }
                return host;
            }
        } catch (Exception ignored) {
        }
        String clean = rawUrl.replaceFirst("^(http[s]?://)", "");
        int slashIdx = clean.indexOf('/');
        if (slashIdx != -1) {
            clean = clean.substring(0, slashIdx);
        }
        if (clean.startsWith("www.")) {
            clean = clean.substring(4);
        }
        return clean;
    }

    public static void detectCategories(String rawUrl, String rawTitle, Set<String> outCategories) {
        String u = (rawUrl != null ? rawUrl : "").toLowerCase();
        String t = (rawTitle != null ? rawTitle : "").toLowerCase();
        String combo = u + " " + t;

        // 1. People Search
        if (combo.contains("people") || combo.contains("person") || combo.contains("dossier")
                || combo.contains("yasni") || combo.contains("profile") || combo.contains("background check")
                || combo.contains("whitepages") || combo.contains("yellowpages") || combo.contains("epieos")) {
            outCategories.add("People Search");
        }

        // 2. Social Media Intelligence
        if (combo.contains("social") || combo.contains("twitter") || combo.contains("tweet")
                || combo.contains("facebook") || combo.contains("instagram") || combo.contains("tiktok")
                || combo.contains("linkedin") || combo.contains("reddit") || combo.contains("telegram")
                || combo.contains("discord") || combo.contains("youtube") || combo.contains("vk.com")
                || combo.contains("toutatis") || combo.contains("osintgram") || combo.contains("affinitweet")) {
            outCategories.add("Social Media Intelligence");
        }

        // 3. Username Investigation
        if (combo.contains("user") || combo.contains("username") || combo.contains("checkusername")
                || combo.contains("namecheckr") || combo.contains("knowem") || combo.contains("sherlock")
                || combo.contains("maigret") || combo.contains("blackbird") || combo.contains("whatsmyname")) {
            outCategories.add("Username Investigation");
        }

        // 4. Email Intelligence
        if (combo.contains("email") || combo.contains("mail") || combo.contains("epieos")
                || combo.contains("hunter.io") || combo.contains("mailbox") || combo.contains("inbox")
                || combo.contains("anonymailer") || combo.contains("haveibeenpwned")) {
            outCategories.add("Email Intelligence");
        }

        // 5. Phone Number Intelligence
        if (combo.contains("phone") || combo.contains("fone") || combo.contains("caller")
                || combo.contains("numberway") || combo.contains("sms") || combo.contains("carrier")
                || combo.contains("fonefinder") || combo.contains("phoneinfoga")) {
            outCategories.add("Phone Number Intelligence");
        }

        // 6. Domain Intelligence
        if (combo.contains("domain") || combo.contains("whois") || combo.contains("registrar")
                || combo.contains("tld") || combo.contains("icann") || combo.contains("domainbigdata")
                || combo.contains("domainsdb") || combo.contains("web-check")) {
            outCategories.add("Domain Intelligence");
        }

        // 7. DNS & Subdomain Enumeration
        if (combo.contains("dns") || combo.contains("subdomain") || combo.contains("nameserver")
                || combo.contains("zone") || combo.contains("amass") || combo.contains("sublist3r")
                || combo.contains("dnsdumpster") || combo.contains("crt.sh") || combo.contains("dig")) {
            outCategories.add("DNS & Subdomain Enumeration");
        }

        // 8. IP Address Intelligence
        if (combo.contains("ip address") || combo.contains("ip lookup") || combo.contains("geoip")
                || combo.contains("asn") || combo.contains("ip-lookup") || combo.contains("browserleaks")
                || combo.contains("centralops") || combo.contains("infosniper") || combo.contains("myip")) {
            outCategories.add("IP Address Intelligence");
        }

        // 9. Network Reconnaissance
        if (combo.contains("network") || combo.contains("traceroute") || combo.contains("ping")
                || combo.contains("port") || combo.contains("bgp") || combo.contains("macvendor")
                || combo.contains("shodan") || combo.contains("censys") || combo.contains("scanner")) {
            outCategories.add("Network Reconnaissance");
        }

        // 10. Geolocation Intelligence
        if (combo.contains("geo") || combo.contains("latitude") || combo.contains("longitude")
                || combo.contains("geocoding") || combo.contains("locator") || combo.contains("cell tower")
                || combo.contains("cell id") || combo.contains("suncalc") || combo.contains("stevemorse")) {
            outCategories.add("Geolocation Intelligence");
        }

        // 11. Maps & Satellite Intelligence
        if (combo.contains("map") || combo.contains("satellite") || combo.contains("earth")
                || combo.contains("openseamap") || combo.contains("flight") || combo.contains("marine")
                || combo.contains("radar") || combo.contains("gis") || combo.contains("planecrashinfo")) {
            outCategories.add("Maps & Satellite Intelligence");
        }

        // 12. Image Intelligence
        if (combo.contains("image") || combo.contains("photo") || combo.contains("picture")
                || combo.contains("photobucket") || combo.contains("forensic") || combo.contains("fotoforensics")
                || combo.contains("amped") || combo.contains("29a.ch")) {
            outCategories.add("Image Intelligence");
        }

        // 13. Reverse Image Search
        if (combo.contains("reverse image") || combo.contains("saucenao") || combo.contains("tineye")
                || combo.contains("yandex image") || combo.contains("image source locator")) {
            outCategories.add("Reverse Image Search");
        }

        // 14. Video & Media Intelligence
        if (combo.contains("video") || combo.contains("stream") || combo.contains("multimedia")
                || combo.contains("radio.garden") || combo.contains("audio") || combo.contains("radio")) {
            outCategories.add("Video & Media Intelligence");
        }

        // 15. Metadata Analysis
        if (combo.contains("exif") || combo.contains("metadata") || combo.contains("verexif")
                || combo.contains("exif-viewer") || combo.contains("exifdata")) {
            outCategories.add("Metadata Analysis");
        }

        // 16. Dark Web Monitoring Resources
        if (combo.contains("dark web") || combo.contains("darknet") || combo.contains("onion")
                || combo.contains("tor") || combo.contains("hidden service") || combo.contains("deep web")) {
            outCategories.add("Dark Web Monitoring Resources");
        }

        // 17. Threat Intelligence
        if (combo.contains("threat") || combo.contains("cve") || combo.contains("alienvault")
                || combo.contains("otx") || combo.contains("virustotal") || combo.contains("urlscan")
                || combo.contains("abuse") || combo.contains("phish") || combo.contains("security")
                || combo.contains("vulnerability") || combo.contains("spiderfoot") || combo.contains("chaos")) {
            outCategories.add("Threat Intelligence");
        }

        // 18. Malware Analysis Resources
        if (combo.contains("malware") || combo.contains("virus") || combo.contains("bazaar")
                || combo.contains("botnet") || combo.contains("ransomware") || combo.contains("sandbox")) {
            outCategories.add("Malware Analysis Resources");
        }

        // 19. Data Breach Research
        if (combo.contains("breach") || combo.contains("pwned") || combo.contains("leak")
                || combo.contains("breachalarm") || combo.contains("compromised") || combo.contains("dump")) {
            outCategories.add("Data Breach Research");
        }

        // 20. Digital Forensics
        if (combo.contains("forensic") || combo.contains("cyberchef") || combo.contains("aperisolve")
                || combo.contains("steganography") || combo.contains("29a.ch/photo-forensics")) {
            outCategories.add("Digital Forensics");
        }

        // 21. Archive & Historical Research
        if (combo.contains("archive") || combo.contains("wayback") || combo.contains("historical")
                || combo.contains("archive-it") || combo.contains("cache")) {
            outCategories.add("Archive & Historical Research");
        }

        // 22. Search Engines & Google Dorks
        if (combo.contains("google hacking") || combo.contains("dork") || combo.contains("dorksearch")
                || combo.contains("search engine") || combo.contains("intelx") || combo.contains("dataset")) {
            outCategories.add("Search Engines & Google Dorks");
        }

        // 23. Cryptocurrency Intelligence
        if (combo.contains("crypto") || combo.contains("bitcoin") || combo.contains("ethereum")
                || combo.contains("blockchain") || combo.contains("etherscan") || combo.contains("breadcrumbs")
                || combo.contains("wallet")) {
            outCategories.add("Cryptocurrency Intelligence");
        }

        // 24. Company & Business Intelligence
        if (combo.contains("company") || combo.contains("business") || combo.contains("corporate")
                || combo.contains("trademark") || combo.contains("patent")) {
            outCategories.add("Company & Business Intelligence");
        }

        // 25. Public Records Research
        if (combo.contains("public record") || combo.contains("court") || combo.contains("government")
                || combo.contains("aleph") || combo.contains("datasetsearch") || combo.contains("official")) {
            outCategories.add("Public Records Research");
        }

        // 26. Developer & Code Intelligence
        if (combo.contains("github") || combo.contains("gitlab") || combo.contains("repository")
                || combo.contains("api") || combo.contains("developer") || combo.contains("code")) {
            outCategories.add("Developer & Code Intelligence");
        }
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getIconName() {
        return iconName;
    }

    public String getDomain() {
        return domain;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public String getCategory() {
        return primaryCategory;
    }

    public boolean isRecent() {
        return recent;
    }

    public String getAddedDate() {
        return addedDate;
    }

    public String getToolType() {
        return toolType;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public static final String[] ALL_CATEGORIES = new String[]{
            "All Tools",
            "Recently Added",
            "Favorites",
            "People Search",
            "Social Media Intelligence",
            "Username Investigation",
            "Email Intelligence",
            "Phone Number Intelligence",
            "Domain Intelligence",
            "DNS & Subdomain Enumeration",
            "IP Address Intelligence",
            "Network Reconnaissance",
            "Geolocation Intelligence",
            "Maps & Satellite Intelligence",
            "Image Intelligence",
            "Reverse Image Search",
            "Video & Media Intelligence",
            "Metadata Analysis",
            "Dark Web Monitoring Resources",
            "Threat Intelligence",
            "Malware Analysis Resources",
            "Data Breach Research",
            "Digital Forensics",
            "Archive & Historical Research",
            "Search Engines & Google Dorks",
            "Cryptocurrency Intelligence",
            "Company & Business Intelligence",
            "Public Records Research",
            "Developer & Code Intelligence",
            "General Investigation Tools"
    };

    public boolean hasCategory(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            return true;
        }
        String clean = cleanCategoryName(categoryName);
        if (clean.equals("All Tools") || clean.equals("All")) {
            return true;
        }
        if (clean.equals("Recently Added")) {
            return isRecent();
        }
        if (clean.equals("Favorites")) {
            return isFavorite();
        }

        for (String cat : categories) {
            if (cat.equalsIgnoreCase(clean)) {
                return true;
            }
        }
        return false;
    }

    public static String cleanCategoryName(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        for (String cat : ALL_CATEGORIES) {
            if (s.endsWith(cat) || s.equalsIgnoreCase(cat)) {
                return cat;
            }
        }
        int i = 0;
        while (i < s.length() && !Character.isLetterOrDigit(s.charAt(i))) {
            i++;
        }
        return s.substring(i).trim();
    }

    public boolean matches(String query) {
        if (query == null || query.trim().isEmpty() || query.equals(".")) {
            return true;
        }
        String q = query.trim().toLowerCase();
        if (title.toLowerCase().contains(q)
                || url.toLowerCase().contains(q)
                || domain.toLowerCase().contains(q)
                || toolType.toLowerCase().contains(q)
                || addedDate.toLowerCase().contains(q)) {
            return true;
        }
        for (String cat : categories) {
            if (cat.toLowerCase().contains(q)) {
                return true;
            }
        }

        try {
            SiteStatusManager mgr = SiteStatusManager.getInstance(null);
            if (mgr != null) {
                SiteStatus status = mgr.getStatus(url);
                if (status != null && status.getDisplayName().toLowerCase().contains(q)) {
                    return true;
                }
                PricingType pricing = mgr.getPricing(url);
                if (pricing != null && pricing.getDisplayName().toLowerCase().contains(q)) {
                    return true;
                }
            }
        } catch (Exception ignored) {}

        String author = getAuthorUsername();
        if (!author.isEmpty() && author.toLowerCase().contains(q)) {
            return true;
        }

        return false;
    }

    public String getAuthorUsername() {
        if (url == null || url.isEmpty()) return "";
        String lowerUrl = url.toLowerCase();

        if (lowerUrl.contains("github.com/")) {
            int idx = lowerUrl.indexOf("github.com/");
            String path = url.substring(idx + 11);
            String[] parts = path.split("/");
            if (parts.length > 0 && !parts[0].isEmpty()) {
                return parts[0];
            }
        }

        if (lowerUrl.contains("gitlab.com/")) {
            int idx = lowerUrl.indexOf("gitlab.com/");
            String path = url.substring(idx + 11);
            String[] parts = path.split("/");
            if (parts.length > 0 && !parts[0].isEmpty()) {
                return parts[0];
            }
        }

        if (lowerUrl.contains("web-check.xyz")) {
            return "Lissy93";
        }
        if (lowerUrl.contains("whatsmyname.app")) {
            return "WebBreacher";
        }
        if (lowerUrl.contains("cyberchef.org")) {
            return "gchq";
        }
        if (lowerUrl.contains("haveibeenpwned.com")) {
            return "troyhunt";
        }

        if (title != null && title.contains("by ")) {
            int byIdx = title.lastIndexOf("by ");
            String authorPart = title.substring(byIdx + 3).trim();
            int endIdx = authorPart.indexOf(" ");
            if (endIdx != -1) {
                authorPart = authorPart.substring(0, endIdx);
            }
            authorPart = authorPart.replaceAll("[\\(\\)\\[\\]\\-:]", "").trim();
            if (!authorPart.isEmpty()) {
                return authorPart.startsWith("@") ? authorPart.substring(1) : authorPart;
            }
        }

        return "";
    }

    public String getRepositoryUrl() {
        if (url == null || url.isEmpty()) return "";
        String lowerUrl = url.toLowerCase();

        if (lowerUrl.contains("github.com/") || lowerUrl.contains("gitlab.com/")) {
            return url;
        }

        if (lowerUrl.contains("web-check.xyz")) {
            return "https://github.com/Lissy93/web-check";
        }
        if (lowerUrl.contains("whatsmyname.app")) {
            return "https://github.com/WebBreacher/WhatsMyName";
        }
        if (lowerUrl.contains("cyberchef.org")) {
            return "https://github.com/gchq/CyberChef";
        }
        if (lowerUrl.contains("haveibeenpwned.com")) {
            return "https://github.com/troyhunt";
        }

        String author = getAuthorUsername();
        if (!author.isEmpty()) {
            return "https://github.com/" + author;
        }

        return "";
    }

    public boolean hasOpenSourceAttribution() {
        return !getAuthorUsername().isEmpty();
    }
}
