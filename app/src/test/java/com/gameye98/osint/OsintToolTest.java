package com.gameye98.osint;

import org.junit.Test;
import java.util.Set;
import static org.junit.Assert.*;

public class OsintToolTest {

    @Test
    public void testParseValidLine() {
        String line = "http://checkusernames.com/|CHECKUSERNAMES - Check the use of your brand or username on 160 Social Networks|default";
        OsintTool tool = OsintTool.parseLine(line);

        assertNotNull(tool);
        assertEquals("http://checkusernames.com/", tool.getUrl());
        assertEquals("CHECKUSERNAMES - Check the use of your brand or username on 160 Social Networks", tool.getTitle());
        assertEquals("default", tool.getIconName());
        assertEquals("checkusernames.com", tool.getDomain());
        assertTrue(tool.hasCategory("Username Investigation"));
        assertTrue(tool.hasCategory("Social Media Intelligence"));
    }

    @Test
    public void testParseCustomIcon() {
        String line = "http://ip-lookup.net/|IP Address Lookup (IPv4 & IPv6)|ip-lookup.net-favicon.png";
        OsintTool tool = OsintTool.parseLine(line);

        assertNotNull(tool);
        assertEquals("http://ip-lookup.net/", tool.getUrl());
        assertEquals("ip-lookup.net-favicon.png", tool.getIconName());
        assertEquals("ip-lookup.net", tool.getDomain());
        assertTrue(tool.hasCategory("IP Address Intelligence"));
    }

    @Test
    public void testParseRecentLine() {
        String line = "https://whatsmyname.app/|WhatsMyName - Fast Username Enumeration|whatsmyname.png|Web App / CLI|2026-11-15|Username Investigation,Social Media Intelligence,People Search";
        OsintTool tool = OsintTool.parseRecentLine(line);

        assertNotNull(tool);
        assertEquals("https://whatsmyname.app/", tool.getUrl());
        assertEquals("WhatsMyName - Fast Username Enumeration", tool.getTitle());
        assertEquals("whatsmyname.png", tool.getIconName());
        assertEquals("whatsmyname.app", tool.getDomain());
        assertEquals("Web App / CLI", tool.getToolType());
        assertEquals("2026-11-15", tool.getAddedDate());
        assertTrue(tool.isRecent());

        // Test multi-category membership
        assertTrue(tool.hasCategory("Username Investigation"));
        assertTrue(tool.hasCategory("Social Media Intelligence"));
        assertTrue(tool.hasCategory("People Search"));
        assertTrue(tool.hasCategory("⚡ Recently Added"));
        assertTrue(tool.hasCategory("🌐 All Tools"));
    }

    @Test
    public void testDomainExtraction() {
        OsintTool tool1 = new OsintTool("https://www.whois.com/whois", "Whois Lookup", "default");
        assertEquals("whois.com", tool1.getDomain());

        OsintTool tool2 = new OsintTool("http://stevemorse.org/jcal/latlon.php?x=1", "Steve Morse Geo", "default");
        assertEquals("stevemorse.org", tool2.getDomain());

        OsintTool tool3 = new OsintTool("https://bazaar.abuse.ch/sample/123", "Malware Sample", "default");
        assertEquals("bazaar.abuse.ch", tool3.getDomain());
    }

    @Test
    public void testCategoryClassification() {
        // Threat Intel & Malware
        OsintTool toolThreat = new OsintTool("https://bazaar.abuse.ch/", "MalwareBazaar - Malware sample exchange", "default");
        assertTrue(toolThreat.hasCategory("Malware Analysis Resources") || toolThreat.hasCategory("Threat Intelligence"));

        // Images & Metadata
        OsintTool toolExif = new OsintTool("http://exifdata.com/", "EXIF Data Viewer and photo forensics", "default");
        assertTrue(toolExif.hasCategory("Metadata Analysis") || toolExif.hasCategory("Image Intelligence"));

        // Domains & DNS
        OsintTool toolDns = new OsintTool("https://dnsdumpster.com/", "DNSDumpster - Find & lookup dns records", "default");
        assertTrue(toolDns.hasCategory("DNS & Subdomain Enumeration") || toolDns.hasCategory("Domain Intelligence"));

        // Phone Number
        OsintTool toolPhone = new OsintTool("http://www.fonefinder.net", "Fone Finder phone number query", "default");
        assertTrue(toolPhone.hasCategory("Phone Number Intelligence"));

        // Maps & Satellite
        OsintTool toolMap = new OsintTool("http://map.openseamap.org/", "OpenSeaMap - Online marine map", "default");
        assertTrue(toolMap.hasCategory("Maps & Satellite Intelligence"));

        // Archives
        OsintTool toolArchive = new OsintTool("https://archive.org/web/", "Wayback Machine web archive", "default");
        assertTrue(toolArchive.hasCategory("Archive & Historical Research"));
    }

    @Test
    public void testMultiCategorySupport() {
        // Epieos is Email, Phone, and People search
        OsintTool epieos = new OsintTool("https://epieos.com", "Epieos Reverse Email & Phone Search", "epieos.png");
        Set<String> categories = epieos.getCategories();
        assertTrue(categories.contains("Email Intelligence"));
        assertTrue(categories.contains("Phone Number Intelligence") || categories.contains("People Search"));

        assertTrue(epieos.hasCategory("Email Intelligence"));
        assertTrue(epieos.hasCategory("✉️ Email Intelligence"));
    }

    @Test
    public void testFavoritesHandling() {
        OsintTool tool = new OsintTool("https://virustotal.com", "VirusTotal Analyzer", "default");
        assertFalse(tool.isFavorite());
        assertFalse(tool.hasCategory("Favorites"));
        assertFalse(tool.hasCategory("⭐ Favorites"));

        tool.setFavorite(true);
        assertTrue(tool.isFavorite());
        assertTrue(tool.hasCategory("Favorites"));
        assertTrue(tool.hasCategory("⭐ Favorites"));
    }

    @Test
    public void testQueryMatching() {
        OsintTool tool = new OsintTool("https://dnsdumpster.com/", "DNSDumpster - DNS Recon", "default");

        // Matches by title
        assertTrue(tool.matches("DNSDumpster"));
        assertTrue(tool.matches("recon"));

        // Matches by domain / url
        assertTrue(tool.matches("dnsdumpster.com"));
        assertTrue(tool.matches("https"));

        // Matches by category
        assertTrue(tool.matches("DNS & Subdomain Enumeration"));

        // Wildcard dot or empty
        assertTrue(tool.matches("."));
        assertTrue(tool.matches(""));
        assertTrue(tool.matches(null));

        // Non-matching
        assertFalse(tool.matches("nonexistentterm987654321"));
    }

    @Test
    public void testEmptyAndInvalidLine() {
        assertNull(OsintTool.parseLine(null));
        assertNull(OsintTool.parseLine(""));
        assertNull(OsintTool.parseLine("   "));
        assertNull(OsintTool.parseRecentLine(null));
        assertNull(OsintTool.parseRecentLine(""));
    }

    @Test
    public void testCleanCategoryName() {
        assertEquals("People Search", OsintTool.cleanCategoryName("👥 People Search"));
        assertEquals("Domain Intelligence", OsintTool.cleanCategoryName("🔗 Domain Intelligence"));
        assertEquals("Favorites", OsintTool.cleanCategoryName("⭐ Favorites"));
        assertEquals("Recently Added", OsintTool.cleanCategoryName("⚡ Recently Added"));
        assertEquals("Threat Intelligence", OsintTool.cleanCategoryName("🛡️ Threat Intelligence"));
    }
}
