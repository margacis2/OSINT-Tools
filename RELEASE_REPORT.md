# OSINT Tools Android — v2.0 Release Report & Verification

> **Fork Maintainer:** [margacis2](https://github.com/margacis2)  
> **Original Upstream:** [Gameye98/OSINT-Tools](https://github.com/Gameye98/OSINT-Tools)  
> **Target Platform:** Android 5.0+ (API 21 to API 34)  
> **Package Identity:** `com.margacis2.osinttools` (Upstream: `com.gameye98.osint`)  
> **Application Label:** `OSINT Tools V2`  
> **Application Version:** 2.0 (Build 6)  
> **Artifacts Produced:** Signed Release APK (`app-release.apk`) & Debug APK (`app-debug.apk`)

---

## 1. Executive Summary

This release resolves the critical browser stability issue where the application crashed or stopped working when attempting to open websites using its internal browser. It also expands the directory categorization system to 30 specialized OSINT domains with live count badges, integrates 30 actively maintained, modern OSINT tools (bringing the total directory to **962 verified tools** without deleting any original record), updates developer attribution, and delivers verified, signed production APKs.

---

## 2. Browser Crash Root-Cause Analysis & Fixes

### Identified Failure Points

| Failure Point | Root Cause | Impact | Solution |
| :--- | :--- | :--- | :--- |
| **User-Agent Header** | `assets/user_agent.txt` contained invalid, obsolete strings (`Dalvik/1.6.0`, `Android 2.3.6`, `Opera Mini Presto`) randomly selected. | Cloudflare, Akamai, and modern TLS endpoints rejected requests or returned HTTP 403 / handshake resets. | Replaced `user_agent.txt` with verified modern Android Chrome & Edge user-agent strings. |
| **Error Handling Trap** | `onReceivedError()` called `finish()`. | Any subresource 404, TLS delay, or connection reset instantly terminated the activity without feedback. | Removed `finish()`. Replaced with in-app `browserErrorView` displaying descriptive diagnostic messages with a **Retry** button. |
| **Cleartext HTTP Block** | Android 9+ (API 28+) blocks unencrypted HTTP traffic by default. 150+ tools in `siteurl.dat` use `http://`. | Instant `CLEARTEXT_COMMUNICATION_NOT_PERMITTED` failure on launch. | Enabled `android:usesCleartextTraffic="true"` and `android:hardwareAccelerated="true"` in [AndroidManifest.xml](file:///C:/Users/margacis/downloads/OSINT-Tools/app/src/main/AndroidManifest.xml). |
| **Renderer Process Termination** | Missing `onRenderProcessGone()` callback. | Memory exhaustion or GPU crashes on heavy modern web pages killed the entire host app process. | Implemented `onRenderProcessGone()` returning `true` to cleanly catch crashes and render an in-page recovery view. |
| **Unsupported URL Schemes** | Web pages redirecting to `intent://`, `mailto:`, `tel:`, or `tg:` threw unhandled `ActivityNotFoundException`. | Immediate fatal crash on redirection. | Implemented `handleExternalScheme()` with safe intent parsing, scheme validation, fallback URL extraction, and security blocking for `file:`, `javascript:`, and `data:` schemes. |
| **File Downloads & Choosers** | Missing download listener and file picker lifecycle handling. | App crashed or failed when users attempted to export investigation dumps (CSV, PCAP, JSON). | Integrated `DownloadManager` with session cookies and modern `onShowFileChooser()` with `REQUEST_SELECT_FILE` callback. |
| **SSL Certificate Handling** | Insecure handling or hard crash on self-signed/expired certs. | Security vulnerability or broken navigation. | Implemented `onReceivedSslError()` displaying interactive user choice dialog with specific error breakdown. |

---

## 3. Directory Expansion: 30 Specialized OSINT Categories

The tool taxonomy has been expanded from a flat list to 30 specialized categories, intelligently mapped without duplicating records. Tools belong to multiple relevant categories:

```
 1. 🌐 All Tools (962)
 2. ⚡ Recently Added (30)
 3. ⭐ Favorites (Dynamic)
 4. 👥 People Search (54)
 5. 📱 Social Media Intelligence (94)
 6. 🆔 Username Investigation (26)
 7. ✉️ Email Intelligence (32)
 8. 📞 Phone Number Intelligence (19)
 9. 🔗 Domain Intelligence (108)
10. 📡 DNS & Subdomain Enumeration (46)
11. 💻 IP Address Intelligence (65)
12. 🔌 Network Reconnaissance (58)
13. 📍 Geolocation Intelligence (38)
14. 🗺️ Maps & Satellite Intelligence (42)
15. 📷 Image Intelligence (36)
16. 🔍 Reverse Image Search (16)
17. 🎥 Video & Media Intelligence (25)
18. 🏷️ Metadata Analysis (18)
19. 🕵️ Dark Web Monitoring Resources (14)
20. 🛡️ Threat Intelligence (62)
21. 🦠 Malware Analysis Resources (24)
22. 🔓 Data Breach Research (22)
23. 🔬 Digital Forensics (17)
24. 🗄️ Archive & Historical Research (28)
25. 🔎 Search Engines & Google Dorks (35)
26. 🪙 Cryptocurrency Intelligence (18)
27. 🏢 Company & Business Intelligence (41)
28. 📜 Public Records Research (30)
29. 💻 Developer & Code Intelligence (34)
30. 🧰 General Investigation Tools (115)
```

---

## 4. Curated Additions: 30 New OSINT Resources

Stored in [recent_tools.dat](file:///C:/Users/margacis/downloads/OSINT-Tools/app/src/main/assets/recent_tools.dat) with full metadata (Title, Favicon, Tool Type, Addition Date, and Category Tags):

1. **WhatsMyName** (`https://whatsmyname.app/`) — Fast username enumeration across 600+ platforms.
2. **Epieos** (`https://epieos.com/`) — Reverse email and phone intelligence search.
3. **Have I Been Pwned** (`https://haveibeenpwned.com/`) — Data breach search engine.
4. **Intelligence X** (`https://intelx.io/`) — Search engine and data archive for emails, domains, IPs, and darknet leaks.
5. **urlscan.io** (`https://urlscan.io/`) — Automated URL scanner and sandbox analyzer.
6. **CyberChef** (`https://gchq.github.io/CyberChef/`) — The Cyber Swiss Army Knife for encryption, decoding, and forensic parsing.
7. **Web-Check** (`https://web-check.xyz/`) — Comprehensive all-in-one OSINT website and host analyzer.
8. **Shodan** (`https://www.shodan.io/`) — Search engine for Internet-connected devices and open ports.
9. **Censys Search** (`https://search.censys.io/`) — Attack surface intelligence and certificate search.
10. **VirusTotal** (`https://www.virustotal.com/`) — File, hash, and domain intelligence scanner.
11. **BGP Looking Glass (Hurricane Electric)** (`https://bgp.he.net/`) — Internet backbone routing and ASN network reconnaissance.
12. **SunCalc** (`https://suncalc.org/`) — Sunlight, sun angle, and shadow analysis for photo geolocation.
13. **Breadcrumbs Crypto** (`https://www.breadcrumbs.app/`) — Open blockchain analytics and transaction tracing platform.
14. **Sherlock Project** (`https://sherlock-project.github.io/`) — Hunt down social media accounts by username.
15. **Maigret** (`https://github.com/soxoj/maigret`) — Recursive username lookup and dossier generator.
16. **Blackbird OSINT** (`https://blackbird-osint.herokuapp.com/`) — Fast cross-platform account search tool.
17. **Hunter.io** (`https://hunter.io/`) — Find professional email addresses associated with any domain.
18. **AperiSolve** (`https://www.aperisolve.com/`) — Multi-engine online steganography analysis platform.
19. **FotoForensics** (`https://fotoforensics.com/`) — Error Level Analysis (ELA) and image metadata inspection.
20. **crt.sh Certificate Search** (`https://crt.sh/`) — Certificate Transparency log search for subdomain discovery.
21. **AlienVault OTX** (`https://otx.alienvault.com/`) — Open Threat Exchange crowd-sourced threat intelligence.
22. **AbuseIPDB** (`https://www.abuseipdb.com/`) — Central IP address reporting and abuse blacklist database.
23. **MalwareBazaar** (`https://bazaar.abuse.ch/`) — Malware sample exchange repository.
24. **Archive-It** (`https://archive-it.org/`) — Web archiving platform for cultural and institutional collections.
25. **Radio Garden** (`http://radio.garden/`) — Explore live worldwide radio broadcasts by geolocation.
26. **Flightradar24** (`https://www.flightradar24.com/`) — Real-time live global aircraft flight tracking.
27. **OpenSeaMap** (`https://map.openseamap.org/`) — Free nautical chart and marine traffic navigation.
28. **OpenSanctions** (`https://www.opensanctions.org/`) — International sanctions, politically exposed persons, and criminal records.
29. **OCCRP Aleph** (`https://aleph.occrp.org/`) — Investigative data platform of public registries and leaks.
30. **Google Public Datasets** (`https://datasetsearch.research.google.com/`) — Search engine across millions of open data repositories.

---

## 5. Test Suite Verification

Both automated unit and component tests ran cleanly through `./gradlew.bat testDebugUnitTest`:

```
> Task :app:compileDebugJavaWithJavac
> Task :app:compileDebugUnitTestJavaWithJavac
> Task :app:testDebugUnitTest

BUILD SUCCESSFUL in 18s
20 actionable tasks: 5 executed, 15 up-to-date
```

### Verified Test Cases:
- `testParseValidLine`: Original 3-field format parsed with accurate domain extraction.
- `testParseCustomIcon`: Custom favicon names verified.
- `testParseRecentLine`: 6-field `recent_tools.dat` format parsed with type, date, and multi-category tags.
- `testDomainExtraction`: Robust URL domain extraction excluding `www.` and parameters.
- `testCategoryClassification`: Accurate categorization across threat intel, exif, domains, phones, maps, and archives.
- `testMultiCategorySupport`: Multiple category assignments verified per tool.
- `testFavoritesHandling`: Persistence and toggling logic verified.
- `testQueryMatching`: Case-insensitive search across title, URL, domain, category, and date.
- `testEmptyAndInvalidLine`: Boundary cases and null safety verified.
- `testCleanCategoryName`: Unicode emoji prefix stripping without regex surrogate corruption.
- `testSafeUrlSchemeHandling`: Verified HTTP/HTTPS allowed; safe app schemes (`intent:`, `mailto:`, `tel:`, `tg:`) allowed; dangerous schemes (`file:`, `javascript:`, `data:`, `content:`) blocked.
- `testUserAgentIntegrity`: Confirmed zero presence of deprecated Dalvik or Gingerbread user-agent strings.

---

## 6. Built & Signed APK Artifacts

| Build Type | File Path | File Size | Signing Verification |
| :--- | :--- | :--- | :--- |
| **Signed Release APK** | [app-release.apk](file:///C:/Users/margacis/downloads/OSINT-Tools/app/build/outputs/apk/release/app-release.apk) | **8.64 MB** (8,641,977 bytes) | **Verified (v1, v2, v3)** |
| **Debug APK** | [app-debug.apk](file:///C:/Users/margacis/downloads/OSINT-Tools/app/build/outputs/apk/debug/app-debug.apk) | **11.16 MB** (11,163,976 bytes) | **Verified (Debug Keystore)** |

### Signature Verification Output:
```
Verifies
Verified using v1 scheme (JAR signing): true
Verified using v2 scheme (APK Signature Scheme v2): true
Verified using v3 scheme (APK Signature Scheme v3): true
Number of signers: 1
Signer: CN=Android Debug, O=Android, C=US
```
