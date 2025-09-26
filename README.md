# Quartier: Bluetooth Business Card

Quartier is a cross-platform mobile app that lets users instantly share business cards at networking events via Bluetooth.

## Features
- Create personalized business cards (name, title, LinkedIn, etc.)
- Share cards with others nearby using Bluetooth
- Cross-platform: Android & iOS

## Demo
![Demo GIF](assets/QuartierDemo.gif)

## Prerequisites
- [Node.js](https://nodejs.org/)
- [Supabase CLI](https://supabase.com/docs/guides/cli)
- [Android Studio](https://developer.android.com/studio) / [Xcode](https://developer.apple.com/xcode/)

## Installation

```bash
git clone https://github.com/emilet16/Bluetooth-Business-Card.git
cd Bluetooth-Business-Card
```

### Configure Supabase Keys

**Android:**  
Edit `android/gradle.properties`:
```kotlin
SUPABASE_URL_RELEASE = "[Your Supabase URL]"
SUPABASE_KEY_RELEASE = "[Your Supabase Key]"
```

**iOS:**  
Edit `ios/Quartier/QuartierApp.swift`:
```swift
supabaseURL: URL(string: "[Your Supabase URL]")!,
supabaseKey: "[Your Supabase Key]"
```

## Running the App
- Open in Android Studio or Xcode and build/run as usual.
- Start the server:
```bash
npx supabase start
```

## Usage
1. Open Quartier
2. Fill in your details to create your business card
3. Tap '+' to broadcast via Bluetooth
4. Nearby users can receive and save your card

## License
This project is licensed under the MIT License.

## Contact
Questions or feedback? Open an issue or email [turcotte.emile1610@gmail.com](mailto:turcotte.emile1610@gmail.com)