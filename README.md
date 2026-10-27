# Al Rashid IPTV Premium
Android IPTV player project for Android phones and Android TV, written in Kotlin.

Initial features: M3U/M3U8 URL and local-file import, Xtream Codes live-stream import, channel search, favourites, Media3 playback, and GitHub Actions unit tests plus APK artifact.

Limitations: Stalker Portal currently performs a handshake check only; provider-specific channel catalog and playback resolution are incomplete. Favorites and imported channels are not persisted. The requested portrait background image has not yet been added to this repository. A successful CI build does not guarantee compatibility with every provider.

Build: JDK 17, Android SDK 36, Gradle 8.13. Run `gradle testDebugUnitTest assembleDebug`. APK path: `app/build/outputs/apk/debug/app-debug.apk`.