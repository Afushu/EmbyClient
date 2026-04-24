# EmbyClient

Emby third-party client for Android Mobile and TV.

## Features

- **No ads** - Pure and clean viewing experience
- **API 23 compatible** - Works on Android 6.0 and above
- **MVVM architecture** - Modern and maintainable code structure
- **Single package for both TV and Mobile** - Automatic UI adaptation
- **Multi-server support** - Add, edit, save, and switch between multiple Emby servers
- **Complete Emby API integration** - Access movies, TV shows, anime, collections, continue watching, recently added, and favorites
- **Full metadata display** - Posters, backgrounds, synopsis, ratings, cast, and episode lists
- **STRM file support** - Parse and play STRM files from NAS and Synology
- **Playback progress sync** - Local and server-side progress sync across devices
- **Advanced player features** - H264, H265, 4K, HDR, multiple audio tracks, multiple subtitles, speed control, and hardware/software decoding
- **Touch gestures** - Swipe for progress, volume, and brightness control on mobile
- **TV remote control** - Full remote control support with focus management

## Compatibility

- **Minimum Android version**: 6.0 (API 23)
- **Supported devices**: Android phones, tablets, TV boxes, and smart TVs
- **Architectures**: ARM and x86

## Build Instructions

### Prerequisites

- Android Studio 2023.1.1 or later
- Java 17 or later
- Kotlin 1.9.0 or later

### Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/Afushu/EmbyClient.git
   ```

2. Open the project in Android Studio.

3. Sync the project with Gradle files.

4. Build the project:
   - For mobile: `./gradlew assembleDebug`
   - For TV: The same build works for both mobile and TV

5. Run the app on your device or emulator.

## Usage

### First Launch

1. Open the app
2. Enter your Emby server URL (e.g., http://192.168.1.100:8096)
3. Enter your Emby username and password
4. Tap "Login" to connect

### Adding Multiple Servers

1. On the login screen, tap "Server List"
2. Tap "Add Server" to add a new server
3. Enter the server details and login
4. Switch between servers by selecting them from the list

### Playing Media

1. Browse through your media libraries
2. Select a movie or TV show
3. Tap "Play" to start playback
4. Use touch gestures on mobile or remote control on TV to control playback

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License.
