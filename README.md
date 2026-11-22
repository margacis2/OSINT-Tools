# OSINT Tools v2.0

[![Release](https://img.shields.io/github/v/release/margacis2/OSINT-Tools?color=6366F1&label=Release)](https://github.com/margacis2/OSINT-Tools/releases)
[![Platform](https://img.shields.io/badge/platform-Android-22C55E.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/language-Java-blue.svg)](https://www.java.com/en/)
[![License](https://img.shields.io/badge/license-MIT-red.svg)](https://opensource.org/license/mit)
[![Upstream](https://img.shields.io/badge/upstream-Gameye98%2FOSINT--Tools-06B6D4.svg)](https://github.com/Gameye98/OSINT-Tools)

A modern, high-performance, categorized Android directory for discovering and accessing publicly available Open-Source Intelligence (OSINT) resources.

This project is a fork of the original [Gameye98/OSINT-Tools](https://github.com/Gameye98/OSINT-Tools) repository, significantly improved and maintained by [margacis2](https://github.com/margacis2).

---

## Credits & Attribution

- **Original Project & Concept:** [Gameye98](https://github.com/Gameye98) & [BlackHole Security](https://github.com/BlackHoleSecurity)
- **Original Upstream Repository:** [https://github.com/Gameye98/OSINT-Tools](https://github.com/Gameye98/OSINT-Tools)
- **Fork Maintainer:** [margacis2](https://github.com/margacis2)
- **Maintainer GitHub:** [https://github.com/margacis2](https://github.com/margacis2)
- **License:** MIT License

---

## What's New in v2.0

### 1. Robust Internal Browser Architecture
- **Permanent Crash Fix:** Eliminated instant closures caused by premature activity finishes and incompatible legacy User-Agent strings.
- **Modern User-Agent Support:** Up-to-date Chrome/Edge User-Agent headers prevent Cloudflare/WAF handshakes and 403 Forbidden errors.
- **Cleartext Traffic Enabled:** Proper support for legacy HTTP tools under Android 9+.
- **Process Crash Recovery:** Catches WebView renderer termination via `onRenderProcessGone()` without crashing the application.
- **In-App Error Handling:** Interactive diagnostics and retry states replace blank screens and crashes.
- **External Scheme Routing:** Safe handling of `intent:`, `mailto:`, `tel:`, and `tg:` schemes, with security guards blocking `file:`, `javascript:`, and `data:` schemes.
- **Export & Download Support:** Native `DownloadManager` integration with cookie forwarding for CSV, PCAP, and JSON investigative exports.
- **PDF Export:** Print/Save current page as PDF via Android `PrintManager`.

### 2. 30 Specialized OSINT Categories
Intelligently organized taxonomy with real-time count badges on each category chip:
1. 🌐 All Tools (962)
2. ⚡ Recently Added (30)
3. ⭐ Favorites (Dynamic)
4. 👥 People Search
5. 📱 Social Media Intelligence
6. 🆔 Username Investigation
7. ✉️ Email Intelligence
8. 📞 Phone Number Intelligence
9. 🔗 Domain Intelligence
10. 📡 DNS & Subdomain Enumeration
11. 💻 IP Address Intelligence
12. 🔌 Network Reconnaissance
13. 📍 Geolocation Intelligence
14. 🗺️ Maps & Satellite Intelligence
15. 📷 Image Intelligence
16. 🔍 Reverse Image Search
17. 🎥 Video & Media Intelligence
18. 🏷️ Metadata Analysis
19. 🕵️ Dark Web Monitoring Resources
20. 🛡️ Threat Intelligence
21. 🦠 Malware Analysis Resources
22. 🔓 Data Breach Research
23. 🔬 Digital Forensics
24. 🗄️ Archive & Historical Research
25. 🔎 Search Engines & Google Dorks
26. 🪙 Cryptocurrency Intelligence
27. 🏢 Company & Business Intelligence
28. 📜 Public Records Research
29. 💻 Developer & Code Intelligence
30. 🧰 General Investigation Tools

### 3. Expanded Tool Directory (962 Tools)
- Retains 100% of all **932 original tools** and URLs from `siteurl.dat`.
- Incorporates **30 additional curated, actively maintained resources** in `recent_tools.dat` (WhatsMyName, Epieos, Have I Been Pwned, IntelX, urlscan.io, CyberChef, Web-Check, Shodan, Censys, VirusTotal, BGP Looking Glass, SunCalc, Breadcrumbs, Sherlock, Maigret, Blackbird, etc.).
- Multi-category support allows discovering multi-purpose tools without duplicate records.

### 4. Modern Cyber-Intelligence UI/UX
- Premium dark cyber-reconnaissance theme (`#0B1020` / `#111A2E` / `#172238`).
- Smooth, adaptive `RecyclerView` grid for phones, foldables, and tablets.
- Instant search across tool titles, URLs, domains, categories, and addition dates.
- Persistent Favorites system stored securely in local device preferences.
- Tool details dialog with direct copy, system share, browser selection, and metadata badges.

---

## Installation

### Download Pre-built APK

Download the latest signed Release APK from GitHub Releases:

👉 **[Download OSINT Tools v2.0 Release APK](https://github.com/margacis2/OSINT-Tools/releases/tag/v2.0)**

#### Sideloading Steps:
1. Download `app-release.apk` directly to your Android device (Android 5.0 Lollipop or higher required).
2. Open your device's file manager or downloads notification and tap the APK.
3. If prompted, permit "Install unknown apps" for your browser or file manager.
4. Tap **Install** and launch OSINT Tools.

---

## Building from Source

### Prerequisites
- JDK 17
- Android SDK (API 34, build-tools 34.0.0)
- Gradle 8.11+ (included via Gradle Wrapper)

### Build Commands
```bash
# Clone the repository
git clone https://github.com/margacis2/OSINT-Tools.git
cd OSINT-Tools

# Run unit tests
./gradlew testDebugUnitTest

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease
```

Generated APKs will be located in:
- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`

---

## License

This project is licensed under the [MIT License](LICENSE).  
Copyright (c) 2026 Gameye98, BlackHole Security & margacis2.
