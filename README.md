# Bluetooth Business Card (Quartier)

Quartier is a mobile application to connect at networking events.

You can:
- Create a business card with your name, title, and LinkedIn
- Share it with other people

## Usage

You can start your own server with the Supabase CLI:

```bash
npx supabase start
```

You might have to change the server URL/Key in the code:

For Android, in android/gradle.properties:

```kotlin
SUPABASE_URL_RELEASE = "[Link to your URL]"
SUPABASE_KEY_RELEASE = "[Supabase Key]"
```

For iOS, in ios/Quartier/QuartierApp.swift

```swift
supabaseURL: URL(string: "[Link to your URL]")!,
supabaseKey: "[Supabase Key]"
```

Then, you can build and run the app in Android IDE or XCode