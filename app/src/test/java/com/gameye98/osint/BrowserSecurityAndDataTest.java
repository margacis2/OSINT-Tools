package com.gameye98.osint;

import org.junit.Test;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class BrowserSecurityAndDataTest {

    @Test
    public void testSafeUrlSchemeHandling() {
        // Safe web schemes
        assertTrue(Browser.isSafeWebScheme("http://example.com"));
        assertTrue(Browser.isSafeWebScheme("https://whois.com"));
        assertFalse(Browser.isSafeWebScheme("javascript:alert(1)"));
        assertFalse(Browser.isSafeWebScheme("file:///android_asset/secret.txt"));
        assertFalse(Browser.isSafeWebScheme("content://media/external/images"));
        assertFalse(Browser.isSafeWebScheme("data:text/html,<html>test</html>"));

        // External app schemes
        assertTrue(Browser.isSupportedExternalScheme("intent://scan/#Intent;scheme=zxing;package=com.google.zxing.client.android;end"));
        assertTrue(Browser.isSupportedExternalScheme("mailto:contact@security.org"));
        assertTrue(Browser.isSupportedExternalScheme("tel:+1234567890"));
        assertTrue(Browser.isSupportedExternalScheme("tg:resolve?domain=bhsecr"));
        assertFalse(Browser.isSupportedExternalScheme("unknownscheme://do/evil"));
    }

    @Test
    public void testUserAgentIntegrity() throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("user_agent.txt");
        if (stream == null) {
            // Alternatively test file directly or via thread context
            stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("assets/user_agent.txt");
        }
        if (stream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                // Verify no obsolete user agents causing SSL handshake failures
                assertFalse(line.contains("Dalvik"));
                assertFalse(line.contains("Android 2.3"));
                assertFalse(line.contains("Opera Mini"));
                assertTrue(line.contains("Mozilla/5.0"));
            }
            reader.close();
        }
    }
}
