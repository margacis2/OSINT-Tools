package com.gameye98.osint;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import static org.junit.Assert.*;

public class SiteCheckerTest {

    @Test
    public void testClassifyPricingFromUrl() {
        assertEquals(PricingType.OPEN_SOURCE, SiteChecker.classifyPricing("https://github.com/Gameye98/OSINT-Tools", ""));
        assertEquals(PricingType.OPEN_SOURCE, SiteChecker.classifyPricing("https://gitlab.com/project/tool", ""));
        assertEquals(PricingType.OPEN_SOURCE, SiteChecker.classifyPricing("https://codeberg.org/user/repo", ""));
        assertEquals(PricingType.OPEN_SOURCE, SiteChecker.classifyPricing("https://bitbucket.org/team/repo", ""));
    }

    @Test
    public void testClassifyPricingFromBody() {
        assertEquals(PricingType.OPEN_SOURCE, SiteChecker.classifyPricing("https://example.com", "This tool is open-source under the MIT license."));
        assertEquals(PricingType.OPEN_SOURCE, SiteChecker.classifyPricing("https://example.com", "Licensed under Apache 2.0. View source code."));
        assertEquals(PricingType.OPEN_SOURCE, SiteChecker.classifyPricing("https://example.com", "Distributed under GNU General Public License v3."));

        assertEquals(PricingType.FREEMIUM, SiteChecker.classifyPricing("https://example.com", "Check our pricing and plans starting at $10/mo with free trial."));
        assertEquals(PricingType.FREEMIUM, SiteChecker.classifyPricing("https://example.com", "Upgrade to premium plan or enterprise plan today."));

        assertEquals(PricingType.FREE, SiteChecker.classifyPricing("https://example.com", "Welcome to the web investigation portal."));
        assertEquals(PricingType.UNKNOWN, SiteChecker.classifyPricing("https://example.com", null));
        assertEquals(PricingType.UNKNOWN, SiteChecker.classifyPricing("https://example.com", ""));
    }

    @Test
    public void testSiteStatusPriorities() {
        // Working must be top (1), Dead must be bottom (6)
        assertTrue(SiteStatus.WORKING.getSortPriority() < SiteStatus.BLOCKED_OR_LOGIN.getSortPriority());
        assertTrue(SiteStatus.BLOCKED_OR_LOGIN.getSortPriority() < SiteStatus.PENDING.getSortPriority());
        assertTrue(SiteStatus.PENDING.getSortPriority() < SiteStatus.TIMEOUT.getSortPriority());
        assertTrue(SiteStatus.TIMEOUT.getSortPriority() < SiteStatus.ERROR.getSortPriority());
        assertTrue(SiteStatus.ERROR.getSortPriority() < SiteStatus.DEAD.getSortPriority());

        assertEquals(1, SiteStatus.WORKING.getSortPriority());
        assertEquals(6, SiteStatus.DEAD.getSortPriority());
    }

    @Test
    public void testStatusSortingWorkingOnTopDeadOnBottom() {
        List<SiteCheckResult> results = new ArrayList<>();
        results.add(new SiteCheckResult("https://dead.com", SiteStatus.DEAD, 404, "https://dead.com", PricingType.UNKNOWN, 0));
        results.add(new SiteCheckResult("https://working2.com", SiteStatus.WORKING, 200, "https://working2.com", PricingType.FREE, 0));
        results.add(new SiteCheckResult("https://error.com", SiteStatus.ERROR, 500, "https://error.com", PricingType.UNKNOWN, 0));
        results.add(new SiteCheckResult("https://working1.com", SiteStatus.WORKING, 200, "https://working1.com", PricingType.OPEN_SOURCE, 0));
        results.add(new SiteCheckResult("https://blocked.com", SiteStatus.BLOCKED_OR_LOGIN, 403, "https://blocked.com", PricingType.UNKNOWN, 0));

        Collections.sort(results, new Comparator<SiteCheckResult>() {
            @Override
            public int compare(SiteCheckResult r1, SiteCheckResult r2) {
                return Integer.compare(r1.getStatus().getSortPriority(), r2.getStatus().getSortPriority());
            }
        });

        assertEquals(SiteStatus.WORKING, results.get(0).getStatus());
        assertEquals(SiteStatus.WORKING, results.get(1).getStatus());
        assertEquals(SiteStatus.BLOCKED_OR_LOGIN, results.get(2).getStatus());
        assertEquals(SiteStatus.ERROR, results.get(3).getStatus());
        assertEquals(SiteStatus.DEAD, results.get(4).getStatus());
    }

    @Test
    public void testStatusParsing() {
        assertEquals(SiteStatus.WORKING, SiteStatus.fromString("working"));
        assertEquals(SiteStatus.WORKING, SiteStatus.fromString("online"));
        assertEquals(SiteStatus.BLOCKED_OR_LOGIN, SiteStatus.fromString("blocked_or_login"));
        assertEquals(SiteStatus.BLOCKED_OR_LOGIN, SiteStatus.fromString("protected"));
        assertEquals(SiteStatus.DEAD, SiteStatus.fromString("dead"));
        assertEquals(SiteStatus.DEAD, SiteStatus.fromString("offline"));
        assertEquals(SiteStatus.TIMEOUT, SiteStatus.fromString("timeout"));
        assertEquals(SiteStatus.ERROR, SiteStatus.fromString("error"));
        assertEquals(SiteStatus.PENDING, SiteStatus.fromString(null));
        assertEquals(SiteStatus.PENDING, SiteStatus.fromString("invalid"));
    }

    @Test
    public void testPricingParsing() {
        assertEquals(PricingType.FREE, PricingType.fromString("free"));
        assertEquals(PricingType.FREEMIUM, PricingType.fromString("freemium"));
        assertEquals(PricingType.OPEN_SOURCE, PricingType.fromString("open_source"));
        assertEquals(PricingType.OPEN_SOURCE, PricingType.fromString("opensource"));
        assertEquals(PricingType.PAID, PricingType.fromString("paid"));
        assertEquals(PricingType.UNKNOWN, PricingType.fromString(null));
        assertEquals(PricingType.UNKNOWN, PricingType.fromString("unknown"));
    }

    @Test
    public void testOpenSourceAuthorAttribution() {
        OsintTool tool1 = new OsintTool("https://github.com/Lissy93/web-check", "web-check by Lissy93", "default");
        assertEquals("Lissy93", tool1.getAuthorUsername());
        assertEquals("https://github.com/Lissy93/web-check", tool1.getRepositoryUrl());
        assertTrue(tool1.hasOpenSourceAttribution());

        OsintTool tool2 = new OsintTool("https://web-check.xyz/", "Web-Check All-in-One", "default");
        assertEquals("Lissy93", tool2.getAuthorUsername());
        assertEquals("https://github.com/Lissy93/web-check", tool2.getRepositoryUrl());
        assertTrue(tool2.hasOpenSourceAttribution());

        OsintTool tool3 = new OsintTool("https://github.com/sherlock-project/sherlock", "Sherlock", "default");
        assertEquals("sherlock-project", tool3.getAuthorUsername());
        assertTrue(tool3.hasOpenSourceAttribution());
    }
}
