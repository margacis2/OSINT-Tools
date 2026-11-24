# OSINT Tools V2

<p align="center">
  <b>A modern Android application for discovering and accessing Open-Source Intelligence (OSINT) resources.</b><br>
  Search, explore, organize, and access hundreds of publicly available intelligence resources from one place.
</p>

<p align="center">
  <a href="https://github.com/margacis2/OSINT-Tools/releases">
    <img src="https://img.shields.io/github/v/release/margacis2/OSINT-Tools?style=flat-square&color=6366f1" alt="Latest Release">
  </a>
  <a href="https://github.com/margacis2/OSINT-Tools/stargazers">
    <img src="https://img.shields.io/github/stars/margacis2/OSINT-Tools?style=flat-square" alt="Stars">
  </a>
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Language-Java-orange?style=flat-square&logo=openjdk&logoColor=white" alt="Java">
  <a href="https://github.com/Gameye98/OSINT-Tools">
    <img src="https://img.shields.io/badge/Based%20on-Gameye98-blue?style=flat-square" alt="Original Project">
  </a>
</p>

## Overview

OSINT Tools V2 is a redesigned and expanded fork of the original [Gameye98/OSINT-Tools](https://github.com/Gameye98/OSINT-Tools) Android project.

It provides a categorized directory of publicly accessible OSINT websites and investigation resources. Instead of manually searching for individual services, users can browse resources by category, search for specific tools, save favorites, and open websites through an integrated browser.

This version focuses on improving the Android user experience, organizing resources, and making the application easier to navigate.

---

## Screenshots

<p align="center">
  <img src="screenshots/home-screen.png" width="170" alt="Home Screen">
  <img src="screenshots/category-filters.png" width="170" alt="Categories">
  <img src="screenshots/favorites-screen.png" width="170" alt="Favorites">
</p>
<p align="center">
  <sub><b>Home</b></sub> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <sub><b>Categories</b></sub> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <sub><b>Favorites</b></sub>
</p>

<p align="center">
  <img src="screenshots/tool-details.png" width="170" alt="Tool Details">
  <img src="screenshots/internal-browser.png" width="170" alt="Internal Browser">
  <img src="screenshots/about-screen.png" width="170" alt="About Screen">
</p>
<p align="center">
  <sub><b>Tool Details</b></sub> &nbsp;&nbsp;&nbsp;&nbsp;
  <sub><b>Browser</b></sub> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <sub><b>About</b></sub>
</p>

---

## Features

### Modern Android Interface
- Redesigned application interface.
- Dark cyber-intelligence visual design with smooth layout animations.
- Responsive tool cards and adaptive grid layout.
- RecyclerView-based resource display with cascade entrance transitions.
- Improved navigation and filtering experience.
- Resource icons and visual details.

### Parallel Site Health Checker & Live Classification (New in v2.0.1)
- High-speed 24-worker parallel site verification engine.
- Instant health detection: **Online (HTTP 2xx/3xx)**, **Protected / Login Wall (Cloudflare/WAF 401/403/429/503)**, and **Offline / Dead Links**.
- Intelligent automatic sorting: **Working sites on top, dead links on bottom**.
- Automated execution when the application is updated.
- Real-time pricing model classification: **Free**, **Freemium**, and **Open Source**.
- In-dialog live re-check trigger for individual websites.

### Open Source Creator & Maintainer Attribution (New in v2.0.1)
- Dedicated creator attribution badges in the tool details dialog.
- Highlights repository authors with their GitHub `@username` and direct repository links.
- Full community attribution for open-source tools included in the directory.

### OSINT Resource Directory
- 932 records in the original resource directory.
- 40 additional modern open data and intelligence resource records.
- 972 total resource records across both data files.
- Resource names, website addresses, and available icons.
- Support for multiple category assignments.

### Search and Filtering
- Search resources by name.
- Search by website URL and domain.
- Filter resources by category.
- Display matching resource counts.
- Reset filters and clear searches.

### Categories
The application provides 27 investigative categories:

| Category | Category |
| :--- | :--- |
| People Search | Social Media Intelligence |
| Username Investigation | Email Intelligence |
| Phone Number Intelligence | Domain Intelligence |
| DNS & Subdomain Enumeration | IP Address Intelligence |
| Network Reconnaissance | Geolocation Intelligence |
| Maps & Satellite Intelligence | Image Intelligence |
| Reverse Image Search | Video & Media Intelligence |
| Metadata Analysis | Dark Web Monitoring Resources |
| Threat Intelligence | Malware Analysis Resources |
| Data Breach Research | Digital Forensics |
| Archive & Historical Research | Search Engines & Google Dorks |
| Cryptocurrency Intelligence | Company & Business Intelligence |
| Public Records Research | Developer & Code Intelligence |
| General Investigation Tools | |

Additional navigation filters:
- All Tools
- Recently Added
- Favorites

### Favorites
- Save useful OSINT resources.
- Remove saved resources.
- Access favorites from a dedicated filter.
- Store favorites locally on the device.

### Resource Details
- View resource name and domain.
- View the complete website address.
- See available category and resource information.
- Copy website URLs.
- Share URLs using Android sharing.
- Add or remove favorites.
- Open resources internally or externally.

### Integrated Browser
- Built-in Android WebView browser.
- Page loading progress.
- Website title and URL display.
- Back and forward navigation.
- Reload and retry functionality.
- Desktop browsing mode.
- File upload support.
- DownloadManager integration.
- External application link handling.
- WebView renderer-crash recovery and error handling.

### Recently Added Resources
An additional collection of 40 resources is maintained separately in `recent_tools.dat`.

Examples include:
- WhatsMyName
- Epieos
- Have I Been Pwned
- Intelligence X
- urlscan.io
- CyberChef
- Web-Check
- Shodan
- Censys
- VirusTotal
- Sherlock
- Maigret
- PhoneInfoga
- SpiderFoot
- Archive.today / Archive.ph
- ViewDNS.info
- Kumu Network Analysis
- OpenSanctions
- CIRCL Hashlookup
- Carrot2
- LittleSis
- Wiby Open Web Search
- World Bank Open Data
- Transparency International

---

## Resource Statistics

| Resource collection | Records |
| :--- | :--- |
| Original directory | 932 |
| Additional modern resources | 40 |
| **Total** | **972** |

*The counts represent records in the application's bundled resource files, not necessarily 962 unique services.*

---

## App Information

| Property | Details |
| :--- | :--- |
| Application name | OSINT Tools V2 |
| Package ID | `com.margacis2.osinttools` |
| Original package ID | `com.gameye98.osint` |
| Platform | Android |
| Language | Java |
| Minimum SDK | 21 |
| Target SDK | 34 |
| Java compatibility | Java 17 |

*Separate installation: OSINT Tools V2 uses a different application ID from the original Gameye98 application. Both applications can therefore be installed independently on the same Android device.*

---

## Download and Installation

Get the available APK builds from:  
👉 **[Download OSINT Tools V2 — GitHub Releases](https://github.com/margacis2/OSINT-Tools/releases)**

1. Open the Releases page.
2. Download the available APK.
3. Open the APK on your Android device.
4. Allow installation from the relevant source if prompted.
5. Install and launch OSINT Tools V2.

*The original Gameye98 application does not need to be uninstalled.*

---

## Build from Source

### Requirements
- JDK 17
- Android Studio or Android SDK
- Android SDK Platform 34
- Gradle Wrapper included in the repository

### Clone the repository
```bash
git clone https://github.com/margacis2/OSINT-Tools.git
cd OSINT-Tools
```

### Run unit tests
**Windows:**
```powershell
.\gradlew.bat testDebugUnitTest
```
**Linux/macOS:**
```bash
./gradlew testDebugUnitTest
```

### Build Debug APK
**Windows:**
```powershell
.\gradlew.bat assembleDebug
```
**Linux/macOS:**
```bash
./gradlew assembleDebug
```

### Build Release APK
**Windows:**
```powershell
.\gradlew.bat assembleRelease
```
**Linux/macOS:**
```bash
./gradlew assembleRelease
```

### APK output
```text
app/build/outputs/apk/
├── debug/
│   └── app-debug.apk
└── release/
    └── app-release.apk
```

*A release build may require the appropriate signing configuration.*

---

## Project Structure

```text
OSINT-Tools/
│
├── app/
│   └── src/
│       ├── main/
│       │   ├── assets/
│       │   │   ├── siteurl.dat
│       │   │   ├── recent_tools.dat
│       │   │   ├── user_agent.txt
│       │   │   └── icons/
│       │   │
│       │   ├── java/
│       │   │   └── com/gameye98/osint/
│       │   │       ├── MainActivity.java
│       │   │       ├── Browser.java
│       │   │       ├── OsintTool.java
│       │   │       ├── ToolAdapter.java
│       │   │       ├── FavoritesManager.java
│       │   │       └── IconManager.java
│       │   │
│       │   └── res/
│       │
│       └── test/
│
├── screenshots/
├── build.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
└── README.md
```

---

## Responsible Use

OSINT Tools V2 provides access to third-party information resources. Users are responsible for how they use these resources.

- Conduct investigations only for lawful and authorized purposes.
- Respect privacy and applicable laws.
- Follow the terms of service of third-party websites.
- Do not use these resources for harassment, stalking, unauthorized access, or other harmful activities.
- Verify information before relying on it.

*This application does not own or operate the listed third-party services and does not guarantee their availability, accuracy, or reliability.*

---

## Credits and Acknowledgements

This project is based on the original work of:
- **Original developer:** [Gameye98](https://github.com/Gameye98)
- **Original repository:** [Gameye98/OSINT-Tools](https://github.com/Gameye98/OSINT-Tools)
- **Original community:** [BlackHole Security](https://github.com/BlackHoleSecurity)
- **Fork maintainer and modifications:** [margacis2](https://github.com/margacis2)

### Open Source Projects & Creator Attributions

We extend our sincere gratitude and credit to the open-source creators and maintainers whose tools, repositories, and intelligence frameworks are featured in this application:

| Tool / Framework | Creator / Maintainer | Repository / Resource |
| :--- | :--- | :--- |
| **Web-Check** | [@Lissy93](https://github.com/Lissy93) (Alicia Sykes) | [Lissy93/web-check](https://github.com/Lissy93/web-check) • [web-check.xyz](https://web-check.xyz) |
| **Sherlock** | [@sherlock-project](https://github.com/sherlock-project) | [sherlock-project/sherlock](https://github.com/sherlock-project/sherlock) |
| **Maigret** | [@soxoj](https://github.com/soxoj) | [soxoj/maigret](https://github.com/soxoj/maigret) |
| **Blackbird** | [@p1ngul1n0](https://github.com/p1ngul1n0) | [p1ngul1n0/blackbird](https://github.com/p1ngul1n0/blackbird) |
| **Social Analyzer** | [@qeeqbox](https://github.com/qeeqbox) | [qeeqbox/social-analyzer](https://github.com/qeeqbox/social-analyzer) |
| **Toutatis / Holehe / Ignorant** | [@megadose](https://github.com/megadose) | [megadose](https://github.com/megadose) |
| **Sublist3r** | [@aboul3la](https://github.com/aboul3la) | [aboul3la/Sublist3r](https://github.com/aboul3la/Sublist3r) |
| **OWASP Amass** | [@owasp-amass](https://github.com/owasp-amass) | [owasp-amass/amass](https://github.com/owasp-amass/amass) |
| **PhoneInfoga** | [@sundowndev](https://github.com/sundowndev) | [sundowndev/PhoneInfoga](https://github.com/sundowndev/PhoneInfoga) |
| **Osintgram** | [@Datalux](https://github.com/Datalux) | [Datalux/Osintgram](https://github.com/Datalux/Osintgram) |
| **SpiderFoot** | [@smicallef](https://github.com/smicallef) | [smicallef/spiderfoot](https://github.com/smicallef/spiderfoot) |
| **theHarvester** | [@laramies](https://github.com/laramies) | [laramies/theHarvester](https://github.com/laramies/theHarvester) |
| **Recon-ng** | [@lanmaster53](https://github.com/lanmaster53) | [lanmaster53/recon-ng](https://github.com/lanmaster53/recon-ng) |
| **GHunt** | [@mxrch](https://github.com/mxrch) | [mxrch/GHunt](https://github.com/mxrch/GHunt) |
| **CyberChef** | [@gchq](https://github.com/gchq) | [gchq/CyberChef](https://github.com/gchq/CyberChef) • [cyberchef.org](https://cyberchef.org) |
| **WhatsMyName** | [@WebBreacher](https://github.com/WebBreacher) | [WebBreacher/WhatsMyName](https://github.com/WebBreacher/WhatsMyName) • [whatsmyname.app](https://whatsmyname.app) |
| **Have I Been Pwned** | [@troyhunt](https://github.com/troyhunt) | [troyhunt](https://github.com/troyhunt) • [haveibeenpwned.com](https://haveibeenpwned.com) |
| **Fierce** | [@mschwager](https://github.com/mschwager) | [mschwager/fierce](https://github.com/mschwager/fierce) |
| **Mailcat** | [@martinvigo](https://github.com/martinvigo) | [martinvigo/mailcat](https://github.com/martinvigo/mailcat) |
| **CrossLinked** | [@m8r0wn](https://github.com/m8r0wn) | [m8sec/CrossLinked](https://github.com/m8r0wn/CrossLinked) |
| **ProtOSINT** | [@pixelbubble](https://github.com/pixelbubble) | [pixelbubble/ProtOSINT](https://github.com/pixelbubble/ProtOSINT) |
| **CloudPeler** | [@zidansec](https://github.com/zidansec) | [zidansec/CloudPeler](https://github.com/zidansec/CloudPeler) |
| **BigDorker** | [@ICWR-TEAM](https://github.com/ICWR-TEAM) | [ICWR-TEAM/BigDorker](https://github.com/ICWR-TEAM/BigDorker) |
| **Rosint** | [@zuxu4n](https://github.com/zuxu4n) | [zuxu4n/Rosint](https://github.com/zuxu4n/Rosint) |
| **Awesome OSINT** | [@jivoi](https://github.com/jivoi) | [jivoi/awesome-osint](https://github.com/jivoi/awesome-osint) |
| **Awesome Hacker Search Engines** | [@edoardottt](https://github.com/edoardottt) | [edoardottt/awesome-hacker-search-engines](https://github.com/edoardottt/awesome-hacker-search-engines) |
| **Awesome Telegram OSINT** | [@ItIsMeCall911](https://github.com/ItIsMeCall911) | [ItIsMeCall911/Awesome-Telegram-OSINT](https://github.com/ItIsMeCall911/Awesome-Telegram-OSINT) |
| **OSINT Stuff Tool Collection** | [@cipher387](https://github.com/cipher387) | [cipher387/osint_stuff_tool_collection](https://github.com/cipher387/osint_stuff_tool_collection) |

All original contributors and repository maintainers retain full copyright and credit for their respective works.

*OSINT Tools V2 is an independently maintained modification of the original project and is not an official release from Gameye98.*

---

## License

The upstream README displays an MIT license badge, but a license file was not verified in the repositories inspected for this README.

Please verify the original project's licensing terms and include the applicable license text before distributing this modified version.

<p align="center">
  <b>OSINT Tools V2</b><br>
  Maintained by <a href="https://github.com/margacis2">margacis2</a><br>
  Based on the original project by Gameye98.
</p>
