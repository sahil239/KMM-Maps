# KMM-Maps

A Kotlin Multiplatform (KMM) sample app demonstrating:

* **Address search** for pickup and drop-off using Google Places Autocomplete
* **Route calculation** using Google Routes API
* **Map display** on Android (Google Maps Compose) and iOS (MapKit) with markers and polylines
* **Composable UI** overlays for search inputs and route info

---

## 📦 Features

* Debounced pickup & drop-off text search
* Swap pickup and drop-off fields
* Clear individual search fields
* Display markers for pickup & drop-off
* Decode and draw route polylines on both platforms
* Show distance (km) and duration (min) in a bottom info bar
* Dotted-line connector between input fields

---

## 📸 App Previews

<table>
  <tr>
    <td>
      <img src="/screenshots/android.gif" height="300" alt="Android Demo"/>
    </td>
    <td>
      <img src="/screenshots/ios.gif" height="300" alt="iOS Demo"/>
    </td>
  </tr>
</table>

> 💡 All screens are fully responsive and work across **Android**, **iOS**, and **Desktop**.
---

## 🚀 Getting Started

1. **Clone the repository**

   ```bash
   git clone https://github.com/your-org/KMM-Maps.git
   cd KMM-Maps
   ```

2. **Add your Google API key**

   Create or update the `local.properties` in the project root with:

   ```properties
   GOOGLE_MAPS_API_KEY=YOUR_GOOGLE_API_KEY_HERE
   ADDRESS_SEARCH_API_KEY=YOUR_GOOGLE_API_KEY_HERE
   ```

   > You can use the same key for both map display and address search if your Google Cloud credentials allow.

3. **Sync & Build**

  * In Android Studio, click **Sync Project with Gradle Files**
  * Run on an Android emulator or device

4. **Run on iOS**

  * Open `iosApp/iosApp.xcworkspace` in Xcode
  * Select a simulator or device and click **Run**

---

## 🗂 Project Structure

```text
KMM-Maps/
├── androidApp/       # Android application module
├── iosApp/           # iOS application module
└── shared/           # KMM shared module
    ├── commonMain/   # Shared code & UI
    ├── androidMain/  # Android-specific implementations
    └── iosMain/      # iOS-specific implementations
```

---

## 🔧 Dependencies

* **Kotlin Multiplatform**
* **Ktor** (HTTP client + JSON)
* **Koin** (DI)
* **moko-permissions** (runtime location permissions)
* **Compose Multiplatform** (common UI)
* **Google Maps Compose** (`com.google.maps.android:maps-compose`)
* **MapKit** (iOS Map integration)

---

## 🎮 Usage

1. Enter **Pick Up** and **Drop Off** addresses in the top fields
2. Select suggestions to place map markers
3. Tap **Go** to compute and display the route
4. View distance & duration in the bottom info bar
5. Use the **Swap** icon to flip pickup & drop-off
6. Clear fields with the **X** icons

---

## 📝 Notes

* Ensure your API key has **Places** and **Routes** APIs enabled in Google Cloud.
* The shared code in `shared/commonMain` is pure Kotlin and runs on Android, iOS, and desktop.

---

## 📖 License

MIT © Sahil Desai
