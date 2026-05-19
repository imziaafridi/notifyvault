# Notify Vault

**Notify Vault** is an Android app that helps you recover deleted WhatsApp messages by listening to notifications and storing them locally on your device.

Built with **Kotlin**, **Jetpack Compose**, **Material 3**, and **Room**.

## Features

- Notification-based message recovery (text and metadata)
- Conversation list with message detail views
- Optional WhatsApp media folder access for attachment recovery
- On-device storage only — no cloud upload
- Guided onboarding for permissions and limitations

## Tech stack

| Layer | Technology |
|-------|------------|
| UI | Jetpack Compose, Material 3 |
| Architecture | ViewModel, Navigation Compose |
| Data | Room, Kotlin Coroutines / Flow |
| Background | NotificationListenerService, Foreground Service |

## Screenshots

_Add screenshots here after capturing from emulator or device._

## Getting started

1. Clone the repository
2. Open in Android Studio (Ladybug or newer recommended)
3. Sync Gradle and run on API 26+ device or emulator
4. Grant **Notification access** when prompted (required for core functionality)

```bash
./gradlew :app:assembleDebug
```

## Privacy

All recovered data stays on the device. See in-app **Privacy Policy** and **Terms of Use**.

## Author

**Zia Afridi** — portfolio project  
Contact: imziaafridi@gmail.com

## License

This project is provided for portfolio and educational purposes. Add your preferred license before public distribution.
