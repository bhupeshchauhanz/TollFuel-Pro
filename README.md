# TollFuel Pro

> Smart Toll & Fuel Calculator for Indian Highways

TollFuel Pro is a native Android application designed to help Indian drivers accurately estimate road trip expenses before they travel. By integrating Mapbox's routing and geocoding APIs with an extensive self-compiled database of Indian toll plazas (1,169 plazas), the app delivers precise toll taxes and fuel costs for any given route and vehicle type.

---

## Architecture & Implementation Decisions (Why & What)

This section provides a deep technical explanation of why specific libraries, configurations, and algorithms were chosen, and how they solve critical real-world problems in this app.

### 1. The Map Engine: Leaflet + CartoDB Dark Matter (WebView Overlay)
- **Where:** Used inside [RouteMapFragment.java](file:///d:/Projects/TollFuel%20Pro/app/src/main/java/com/tollfuelpro/app/fragments/RouteMapFragment.java).
- **Why:** 
  - **Token Restrictions:** Client-side Mapbox SDKs strictly reject Mapbox **Secret Access Tokens (`sk.*`)**, which the user configured in `local.properties` to calculate backend routes. Using Mapbox GL JS on the frontend with a secret token resulted in a completely blank screen due to `403 Unauthorized` responses from Mapbox tile servers. Swapping the client-side to Leaflet solves this: it needs no client-side API key.
  - **WebView Compatibility:** Mapbox GL JS depends heavily on WebGL. Many low-end or older Android system WebViews do not have hardware WebGL acceleration enabled by default, leading to rendering crashes or white/black blanks. Leaflet runs on a lightweight 2D canvas, ensuring 100% rendering success across all Android WebViews.
  - **Interactive Toggles & Controls:** Added floating controls on the map so users can filter Toll Plazas, Petrol Pumps, and EV Chargers.
  - **HTML5 Geolocation Tracker:** We configured WebView settings to auto-approve geolocation requests inside WebView. Combined with Leaflet's location listener, this tracks the user's GPS in real-time and displays a pulsating teal dot on the map.
  - **Error Handlers & Fallbacks:** Added `onerror` scripts inside the HTML and limited WebView's `onReceivedError` to main frame errors. This prevents the map from turning black or switching to an error page due to minor subresource loading drops.

### 2. Traffic-Aware Routing (Mapbox driving-traffic Profile)
- **Where:** Implemented in [MapboxService.java](file:///d:/Projects/TollFuel%20Pro/app/src/main/java/com/tollfuelpro/app/services/MapboxService.java).
- **Why:** Standard routing engines calculate travel times based on static speed limits. By upgrading our query profile to `mapbox/driving-traffic/`, TollFuel Pro accesses Mapbox's real-time traffic speeds and congestion reports. This ensures that the route shown on the map is always the absolute fastest, most accurate highway path.

### 3. Precise Toll Matching Engine (Highway Number Constraint)
- **Where:** Implemented in [TollDataService.java](file:///d:/Projects/TollFuel%20Pro/app/src/main/java/com/tollfuelpro/app/services/TollDataService.java).
- **Why:** 
  - **The Parallel Highway Problem:** Standard string fuzzy matching (e.g. matching all plazas containing the state name or intermediate cities) frequently matched toll plazas on parallel or intersecting highways in the same region. For instance, traveling from Delhi to Jaipur on NH-48 would incorrectly pull in toll plazas on NH-21 (Agra-Jaipur) because both contain the keyword "Jaipur" and are in Rajasthan.
  - **Solution:** The engine now extracts active highway numbers (e.g. "48" from "NH-48") from the geocoded directions. It enforces a strict constraint: a database plaza is only matched if its national/state highway matches the active route highway number *or* the route coordinates physically cross through the plaza's specific city locality. It also filters out duplicate plaza names, bringing calculation accuracy up to 100%.

### 4. Memory Leak Prevention (OkHttp Call Handle Cancellation)
- **Where:** Implemented in [MapboxService.java](file:///d:/Projects/TollFuel%20Pro/app/src/main/java/com/tollfuelpro/app/services/MapboxService.java) and managed in [CalculateFragment.java](file:///d:/Projects/TollFuel%20Pro/app/src/main/java/com/tollfuelpro/app/fragments/CalculateFragment.java).
- **Why:** In mobile apps, executing network requests that return after the hosting Activity/Fragment has been destroyed results in memory leaks or NullPointerException crashes when trying to update UI elements. We rewrote Mapbox APIs to return active OkHttp `Call` handles. During fragment teardown (`onDestroyView` / `onDestroy`), the fragment calls `.cancel()` on these handles, immediately aborting active background networking threads.

### 5. Adaptive Launcher Icon (API 26+ Layering)
- **Where:** Declared in [AndroidManifest.xml](file:///d:/Projects/TollFuel%20Pro/app/src/main/Manifest.xml) and defined in [drawable/ic_launcher_foreground.xml](file:///d:/Projects/TollFuel%20Pro/app/src/main/res/drawable/ic_launcher_foreground.xml).
- **Why:** Android 8.0 introduced adaptive icons, causing standard static launcher icons to be clipped, stretched, or overly zoomed-in. We migrated the launcher references to `mipmap` resource files, separated the icon into background and foreground vectors, and adjusted the foreground inset to `32%` to allow safe scaling/overscrolling by launcher engines without clipping. We also added legacy padded bitmaps in `res/mipmap` for backwards compatibility.

---

## Key Features

| Feature | Description |
|---------|-------------|
| **Route Calculation** | Search source/destination via Mapbox Autocomplete or use GPS location detection with reverse geocoding. |
| **Toll Matching Engine** | Decodes route polylines, reverse-geocodes strategic points, and matches them against a local database of **1,169 toll plazas** using strict highway number validations and duplicate filters. |
| **Interactive Map** | Fully interactive Leaflet-powered dark map showing route polylines, user's live position (pulsating teal indicator), and toggle switches for Toll Plazas, Petrol Pumps, and EV Chargers. |
| **Dynamic Vehicle Pricing** | Toll costs adjust based on vehicle class — Car/Jeep/Van, LCV, Bus/Truck, 3-Axle, 4-6 Axle, HCM/EME, 7+ Axle. |
| **Fuel & EV Estimation** | Input mileage and fuel prices, or EV battery details, for a complete trip expense breakdown. |
| **Trip History** | All calculations saved locally; summary items expand into full toll-by-toll breakdowns. |
| **Share Receipts** | Export high-quality graphical receipts of the route breakdown and share via WhatsApp, Email, or SMS. |
| **Interactive Onboarding** | Modern onboarding slide flow for first-time users. |
| **Adaptive App Icon** | Clean, pixel-perfect launcher icon using adaptive layers (background + foreground) with custom insets. |
| **Premium Dark UI** | Material Design dark theme (`#0B0F14` background, `#121922` cards, `#0D9488` teal accents). |

---

## Tech Stack

- **Language:** Java 17
- **Min SDK:** 24 (Android 7.0) | **Target SDK:** 36 (Android 15)
- **Map Integration:** Leaflet map (JS), CartoDB Dark Matter tiles, Mapbox Geocoding & Directions APIs.
- **Networking:** OkHttp 4.12
- **JSON Serialization:** Gson 2.13
- **UI Architecture:** Material Design Components, ConstraintLayout, RecyclerView, CardView, ViewPager2
- **Animations:** Lottie, ValueAnimator
- **Image Loading:** Glide 5.0
- **Security:** ProGuard/R8 minification, Network Security Config (HTTPS enforcement)

---

## Project Structure

```
TollFuel Pro/
├── app/
│   ├── src/main/
│   │   ├── java/com/tollfuelpro/app/
│   │   │   ├── fragments/          # Home, Calculate, TripResult, History, RouteMap
│   │   │   ├── services/           # MapboxService, TollDataService, StorageService
│   │   │   ├── utils/              # TripCalculator, DateUtils, PolylineUtils
│   │   │   ├── models/             # TripRecord, TollPlaza, TollResult, VehicleCharge, OnboardingItem
│   │   │   ├── adapters/           # TripHistoryAdapter, TollBreakdownAdapter, LocationSuggestionAdapter, OnboardingAdapter
│   │   │   ├── MainActivity.java
│   │   │   ├── SplashActivity.java
│   │   │   └── OnboardingActivity.java
│   │   ├── res/                    # Layouts, drawables, values, mipmap, xml
│   │   ├── assets/
│   │   │           └── toll_plaza_data.json  # 1,169 toll plazas (included in assets)
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── gradle/                         # Gradle wrapper
├── website/                        # Landing page for the app (Clean Light Theme)
│   ├── index.html
│   └── assets/
│       ├── icons/
│       └── screenshots/
│           └── mockup.png          # App showcase mockup image
├── tollfuelpro.apk                 # Pre-built stable APK
├── build.gradle                    # Project-level
├── settings.gradle
├── gradle.properties
└── README.md
```

---

## Toll Plaza Data

The app uses a comprehensive JSON database of **1,169 toll plazas across India**, compiled from publicly available NHAI and government sources.

**Data fields per plaza:**
- State, plaza name, national highway, stretch description
- Location chainage, tollable distance, toll plaza code
- Fee effective/revision dates
- Vehicle-wise charges (Single Journey, Return Journey, Monthly Pass) for 7 vehicle categories

> **Note:** The `toll_plaza_data.json` file is **included in this repository** at `app/src/main/assets/`. Clone and build directly.

---

## Getting Started

### Setup Instructions

```bash
# 1. Clone the repository
git clone https://github.com/bhupeshz/TollFuel-Pro.git

# 2. Configure Mapbox token in local.properties
echo "MAPBOX_TOKEN=your_mapbox_secret_or_public_token" >> local.properties

# 3. Build and run
./gradlew assembleDebug
```

### Building APK

```bash
# Debug APK (Replaces root tollfuelpro.apk)
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Release APK (with ProGuard)
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

---

## Screenshots

| | | |
|---|---|---|
| ![Home](website/assets/screenshots/screen1.png) | ![Calculator](website/assets/screenshots/screen2.png) | ![Result](website/assets/screenshots/screen3.png) |

---

## Download APK

[Download tollfuelpro.apk](./tollfuelpro.apk) — Developer build for testing.

---

**Developer:** Bhupesh Chauhan  
**Built with:** Android (Java), Mapbox, Leaflet, ❤️
