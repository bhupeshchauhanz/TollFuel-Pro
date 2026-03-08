# TollFuel Pro 🚗 🛣️

TollFuel Pro is a native Android application designed to give drivers across India highly accurate estimates of their upcoming trip expenses. By integrating Mapbox's advanced turn-by-turn routing and geocoding capabilities with a comprehensive Indian toll plaza dataset, the app calculates precise toll taxes and fuel costs for any given route and vehicle type.

## 🌟 Key Features

*   **Intelligent Route Calculation:** Search for any source and destination using Mapbox Autocomplete. The app also supports a "Current Location" feature that uses Reverse Geocoding to automatically type out your real-world city/neighborhood based on GPS coordinates.
*   **Geospatial Toll Matching Engine:** Native route polylines are decoded and strategically sampled. These coordinates are reverse-geocoded to build a robust dictionary of geographical keywords. This dictionary is then cross-referenced against a JSON database of toll plazas using fuzzy string matching to ensure tolls on long highway stretches are never missed.
*   **Dynamic Vehicle Pricing:** Toll costs dynamically adjust based on your selected vehicle class (Car/Jeep/Van, LCV, Bus/Truck, etc.).
*   **Fuel Estimation:** Input your vehicle's mileage and the local fuel price to instantly see a breakdown of fuel costs alongside toll taxes.
*   **Trip History Dashboard:** Saves all your previous calculations locally so you can review your recent trips. Trip items are summarized cleanly but expand into full breakdowns when tapped.
*   **Share Visual Receipts:** Want to split costs with friends? The Result screen allows you to export a high-quality graphical screenshot of your route's complete financial breakdown and share it seamlessly via WhatsApp, Email, or SMS using Android's native sharing intents.

## 🛠️ Tech Stack

*   **Language:** Java
*   **Platform:** Android (Min SDK 24, Target SDK 34)
*   **APIs & Integrations:**
    *   Mapbox Search/Geocoding API (Autocomplete & Reverse Geocoding)
    *   Mapbox Directions API (Turn-by-turn navigation & route geometry)
    *   Gson (JSON Parsing & Serialization)
    *   OkHttp (Networking)

## 📁 Project Structure

*   `app/src/main/java/com/tollfuelpro/app/`
    *   `fragments/`: Contains the core UI controllers (`HomeFragment`, `CalculateFragment`, `TripResultFragment`, `HistoryFragment`).
    *   `services/`: Houses the backend logic like `MapboxService` (API calls), `TollDataService` (JSON parsing and toll matching), and `StorageService` (SharedPreferences wrapper for Trip History).
    *   `utils/`: Helper classes like `TripCalculator`, `DateUtils`, and `PolylineUtils` (for decoding Mapbox geometries).
    *   `models/`: Data structures (`TripRecord`, `TollPlaza`, `VehicleCharge`).
*   `app/src/main/assets/`: Contains the offline database `toll_plaza_data.json` *(see Data Source section below)*.
*   `app/src/main/res/xml/`: Contains `provider_paths.xml` configuring the `FileProvider` for secure image sharing.

## 📊 Data Source

The toll plaza dataset used in this app (`toll_plaza_data.json`) was **self-aggregated from publicly available sources** for educational and personal learning purposes only.

> ⚠️ **Important:** The `toll_plaza_data.json` file is **not included** in this repository. You will need to provide your own toll dataset to run this project.

### Why is the data file excluded?

- No official government API or open dataset was available with the required level of detail at the time of development. *(You can check [data.gov.in](https://data.gov.in) for any updated official datasets.)*
- To respect the terms of the sources the data was collected from.
- This project is intended to demonstrate the **application logic and Android architecture**, not to redistribute third-party data.

### How to add your own data

Place a file named `toll_plaza_data.json` inside `app/src/main/assets/` following this structure:

```json
[
  {
    "name": "Toll Plaza Name",
    "highway": "NH-XX",
    "state": "State Name",
    "keywords": ["city1", "city2"],
    "charges": {
      "car": 85,
      "lcv": 135,
      "bus_truck": 290,
      "uav": 310,
      "hcm_emme": 425,
      "seven_axle": 500
    }
  }
]
```

## 🚀 Getting Started

### Prerequisites

*   Android Studio (Iguana or newer recommended)
*   A Mapbox Account and an active **Mapbox Public Access Token**
*   Your own `toll_plaza_data.json` file *(see Data Source section above)*

### Installation

1.  **Clone the repository**
2.  **Open in Android Studio:** Open the `TollFuel Pro` directory.
3.  **Add your toll data:** Place your `toll_plaza_data.json` in `app/src/main/assets/`.
4.  **Configure API Key:**
    *   The app requires a Mapbox token to function. The project is currently configured to read `MAPBOX_TOKEN` from the `BuildConfig`.
    *   You may need to provide this in your `local.properties` or define it within the `build.gradle` file manually before compiling.
5.  **Build and Run:** Sync gradle files and run the application on an emulator or physical device.

---

## ⚠️ Disclaimer

This project was built **purely for learning purposes** — to practice Android development, API integration, geospatial logic, and data parsing. It is not intended for commercial use or redistribution.

---

*Developed with a focus on robust data parsing and premium Android UI design.*