# ScanSafe 🍎 — Know What You Eat

A production-ready Android food scanner app powered by Claude AI.

![Platform](https://img.shields.io/badge/Platform-Android-green)
![SDK](https://img.shields.io/badge/Min%20SDK-26-blue)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024-orange)

---

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Android Setup](#android-setup)
- [Backend Setup](#backend-setup)
- [Firebase Setup (Google Sign-In)](#firebase-setup)
- [API Keys Required](#api-keys-required)
- [Running the App](#running-the-app)
- [API Documentation](#api-documentation)
- [Testing](#testing)

---

## ✨ Features

| Feature | Description |
|---|---|
| 📷 Barcode Scanner | Real-time scanning via CameraX + ML Kit |
| 🤖 AI Analysis | Claude claude-sonnet-4-6 health scoring & ingredient analysis |
| 🟢 Health Score | Animated gauge 1–10 with color coding |
| ⚠️ Allergen Alerts | Automatic detection of 8+ common allergens |
| 🧪 E-number Flags | Additive risk classification (Safe/Moderate/Avoid) |
| 📊 Nutrition Table | Per-100g animated bars with daily reference values |
| ❤️ Favourites | Save products for quick access |
| 📋 History | Full scan history with search & swipe-delete |
| 👤 Profile | Health preferences (Vegan, Gluten-Free, Keto, Diabetic) |
| 🌙 Dark Mode | Full Material3 dark theme |

---

## 🏛️ Architecture

```
Clean Architecture + MVVM

┌─────────────────────────────────────┐
│         Presentation Layer          │
│  (Jetpack Compose + ViewModels)     │
├─────────────────────────────────────┤
│           Domain Layer              │
│    (Use Cases + Repository          │
│        Interfaces + Models)         │
├─────────────────────────────────────┤
│            Data Layer               │
│   (Room DB + Retrofit + DataStore   │
│       + Repository Impls)           │
└─────────────────────────────────────┘
```

---

## 📁 Project Structure

```
Scan_The_Barcode/
├── app/src/main/java/com/scansafe/
│   ├── core/
│   │   ├── di/              # Hilt modules
│   │   ├── network/         # NetworkResult, AuthInterceptor
│   │   └── utils/           # Constants, Extensions
│   ├── data/
│   │   ├── local/           # Room DB, DAOs, Entities, DataStore
│   │   ├── mapper/          # DTO → Domain mappers
│   │   ├── remote/          # Retrofit ApiService, DTOs
│   │   └── repository/      # Repository implementations
│   ├── domain/
│   │   ├── model/           # Domain models
│   │   ├── repository/      # Repository interfaces
│   │   └── usecase/         # All use cases
│   ├── navigation/          # NavGraph, BottomNavBar, Screen
│   ├── presentation/
│   │   ├── auth/            # Login, Register screens
│   │   ├── components/      # Shared composables
│   │   ├── favourites/      # Favourites screen
│   │   ├── history/         # History screen
│   │   ├── onboarding/      # Onboarding slides
│   │   ├── profile/         # Profile screen
│   │   ├── result/          # Product result screen
│   │   ├── scanner/         # Camera scanner screen
│   │   └── splash/          # Splash screen
│   └── ui/theme/            # Color, Typography, Theme
└── backend/
    ├── config/              # MongoDB, Claude API config
    ├── controllers/         # Route handlers
    ├── middleware/          # Auth, error, rate limiting
    ├── models/              # Mongoose schemas
    ├── routes/              # Express routers
    ├── services/            # OpenFoodFacts, USDA APIs
    └── tests/               # Jest integration tests
```

---

## ⚙️ Prerequisites

### Android
- Android Studio Hedgehog or newer
- JDK 17+
- Android device or emulator (API 26+)

### Backend
- Node.js 18+
- npm 9+
- MongoDB Atlas account (free M0 tier is fine)

---

## 📱 Android Setup

### Step 1: Clone and open
```bash
git clone <your-repo-url>
# Open Scan_The_Barcode/ in Android Studio
```

### Step 2: Configure `local.properties`
```properties
sdk.dir=C:\Users\<YOUR_USER>\AppData\Local\Android\Sdk
# For Android Emulator:
BASE_URL=http://10.0.2.2:5000/
# For physical device (replace with your PC's IP):
# BASE_URL=http://192.168.1.100:5000/
```

### Step 3: Add `google-services.json`
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a project named "ScanSafe"
3. Add an Android app with package name `com.scansafe`
4. Download `google-services.json`
5. Place it at: `app/google-services.json`

### Step 4: Add Poppins font files
Download Poppins from [Google Fonts](https://fonts.google.com/specimen/Poppins) and place:
```
app/src/main/res/font/
  ├── poppins_regular.ttf
  ├── poppins_medium.ttf
  ├── poppins_semibold.ttf
  └── poppins_bold.ttf
```

### Step 5: Sync and build
```bash
./gradlew assembleDebug
```

---

## 🖥️ Backend Setup

### Step 1: Navigate to backend
```bash
cd Scan_The_Barcode/backend
```

### Step 2: Install dependencies
```bash
npm install
```

### Step 3: Create `.env` from example
```bash
cp .env.example .env
```

### Step 4: Fill in `.env`
```env
# MongoDB Atlas connection string
MONGODB_URI=mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/scansafe

# JWT secrets (generate with: node -e "console.log(require('crypto').randomBytes(64).toString('hex'))")
JWT_SECRET=your_64_char_secret_here
JWT_REFRESH_SECRET=your_other_64_char_secret_here

# Claude AI
ANTHROPIC_API_KEY=sk-ant-api03-xxxxxxxxxxxx
CLAUDE_MODEL=claude-sonnet-4-6

# USDA (free key at https://fdc.nal.usda.gov/api-guide.html)
USDA_API_KEY=your_usda_key_here

# Firebase Admin (for Google Sign-In)
FIREBASE_PROJECT_ID=your-firebase-project-id
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n"
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxx@your-project.iam.gserviceaccount.com
```

### Step 5: Start the server
```bash
# Development (with hot reload)
npm run dev

# Production
npm start
```

Server starts at `http://localhost:5000`

---

## 🔥 Firebase Setup

### For Google Sign-In (Android)

1. **Firebase Console** → Project Settings → Service Accounts → Generate Private Key
2. Copy the values to `.env` (`FIREBASE_PROJECT_ID`, `FIREBASE_PRIVATE_KEY`, `FIREBASE_CLIENT_EMAIL`)
3. Install Firebase Admin in backend: `npm install firebase-admin`
4. Add to `server.js`:
```js
const admin = require('firebase-admin');
admin.initializeApp({
  credential: admin.credential.cert({
    projectId: process.env.FIREBASE_PROJECT_ID,
    privateKey: process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n'),
    clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
  })
});
```
5. In Android Studio: **Tools → Firebase → Authentication → Sign in with Google** → follow the wizard to add SHA-1 fingerprint

---

## 🔑 API Keys Required

| Service | Get Key | Cost |
|---|---|---|
| **MongoDB Atlas** | [cloud.mongodb.com](https://cloud.mongodb.com) | Free (M0) |
| **Anthropic Claude** | [console.anthropic.com](https://console.anthropic.com) | Pay-per-use |
| **USDA FoodData Central** | [fdc.nal.usda.gov/api-guide](https://fdc.nal.usda.gov/api-guide.html) | Free |
| **Open Food Facts** | No key needed | Free |
| **Firebase** | [console.firebase.google.com](https://console.firebase.google.com) | Free |

---

## 🚀 Running the App

### Option 1: Emulator (recommended for development)
1. Start AVD with API 26+
2. Start backend: `npm run dev`
3. Run app from Android Studio (▶️)

### Option 2: Physical Device
1. Enable USB debugging on your Android phone
2. Find your PC's IP: `ipconfig` (Windows) → IPv4 Address
3. Set `BASE_URL=http://<your-ip>:5000/` in `local.properties`
4. Connect via USB and run

---

## 📡 API Documentation

### Auth
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | ❌ | Register new user |
| POST | `/api/auth/login` | ❌ | Login |
| POST | `/api/auth/logout` | ✅ | Logout |
| GET | `/api/auth/me` | ✅ | Get current user |
| POST | `/api/auth/google` | ❌ | Google Sign-In |

### Product
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/product/:barcode` | ✅ | Get product with AI analysis |

### History
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/history` | ✅ | Get scan history (paginated) |
| POST | `/api/history` | ✅ | Save a scan |
| DELETE | `/api/history/:id` | ✅ | Delete a history entry |

### Favourites
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/favourites` | ✅ | Get all favourites |
| POST | `/api/favourites` | ✅ | Add to favourites |
| DELETE | `/api/favourites/:id` | ✅ | Remove from favourites |

### User
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/user/profile` | ✅ | Get user profile |
| PUT | `/api/user/profile` | ✅ | Update name |
| PUT | `/api/user/preferences` | ✅ | Update health preferences |

All authenticated requests require: `Authorization: Bearer <token>`

---

## 🧪 Testing

### Android Unit Tests
```bash
./gradlew test
```

### Android Instrumented Tests (requires emulator)
```bash
./gradlew connectedAndroidTest
```

### Backend Tests
```bash
cd backend
npm test
```

---

## 🔒 Security Notes

- JWT tokens expire in **7 days** (configurable via `JWT_EXPIRES_IN`)
- Passwords hashed with **bcrypt** (12 rounds)
- API keys stored in **environment variables only** — never committed
- Rate limiting: 100 requests per 15 minutes
- Android: Base URL stored in `local.properties` (excluded from git)
- ProGuard enabled for release builds

---

## 📄 License

MIT License — Built for educational and production use.

---

**Built with ❤️ using Kotlin, Jetpack Compose, Node.js, MongoDB, and Claude AI**
