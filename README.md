# flickr-pickr
A simple Flickr client for Android.

## Requirements

- **Android Studio**: Ladybug or newer
- **JDK**: 21
- **Flickr API Key**: Get one at [Flickr API](https://www.flickr.com/services/api/misc.api_keys.html)

## Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/flickr-pickr.git
   cd flickr-pickr
   ```

2. **Create environment file**

   Create a `.env` file in the project root with your Flickr API key:
   ```
   FLICKR_API_KEY=your_api_key_here
   ```

3. **Open in Android Studio**

   Open the project in Android Studio and let Gradle sync complete.

4. **Run the app**

   Select a device/emulator and click Run.

## Build Commands

```bash
# Build the project
./gradlew build

# Run unit tests
./gradlew testDebugUnitTest

# Run a single test class
./gradlew testDebugUnitTest --tests "dev.engel.flickrpickr.feature.photos.PhotosViewModelTest"

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean build

# Check for lint issues
./gradlew lint
```

## Troubleshooting

### Build fails with ".env file not found"
Create a `.env` file in the project root directory with your Flickr API key:
```
FLICKR_API_KEY=your_api_key_here
```

### Tests fail with "Method e in android.util.Log not mocked"
This should be fixed by the `unitTests.isReturnDefaultValues = true` setting in `app/build.gradle.kts`. If you still see this error, ensure you have the latest version of the build file.

### JUnit 5 tests not running / "No matching tests found"
Ensure your `app/build.gradle.kts` includes:
```kotlin
testOptions {
    unitTests.all {
        it.useJUnitPlatform()
    }
}
```
And the following test dependencies:
```kotlin
testImplementation(platform(libs.junit.bom))
testImplementation(libs.junit.jupiter)
testRuntimeOnly(libs.junit.platform.launcher)
```

### Gradle sync fails with JDK version errors
This project requires JDK 21. In Android Studio:
1. Go to **File > Settings > Build, Execution, Deployment > Build Tools > Gradle**
2. Set **Gradle JDK** to version 21

### Compose preview not rendering
Ensure you have the Compose plugin enabled and try **Build > Make Project** to generate necessary preview classes.

## Dependencies

| Category         | Library                                                                                   | Purpose                                       |
|------------------|-------------------------------------------------------------------------------------------|-----------------------------------------------|
| **Architecture** | [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)         | UI state management with lifecycle awareness  |
|                  | [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)            | Single-activity navigation                    |
|                  | [Hilt](https://dagger.dev/hilt/)                                                          | Compile-time dependency injection             |
|                  | [Coroutines / Flow](https://kotlinlang.org/docs/coroutines-overview.html)                 | Asynchronous programming and reactive streams |
| **Data**         | [Retrofit](https://square.github.io/retrofit/)                                            | Type-safe HTTP client                         |
|                  | [OkHttp](https://square.github.io/okhttp/)                                                | HTTP client with logging interceptor          |
|                  | [kotlinx.serialization](https://kotlinlang.org/docs/serialization.html)                   | JSON parsing                                  |
| **Testing**      | [JUnit 5](https://junit.org/junit5/)                                                      | Unit test framework                           |
|                  | [Coroutines Test](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/) | Testing coroutines and flows                  |
|                  | [MockK](https://mockk.io/)                                                                | Kotlin-first mocking                          |
|                  | [Strikt](https://strikt.io/)                                                              | Fluent assertion library                      |
|                  | [Espresso](https://developer.android.com/training/testing/espresso)                       | UI testing framework                          |
| **UI**           | [Jetpack Compose](https://developer.android.com/jetpack/compose)                          | Declarative UI framework                      |
|                  | [Material 3](https://developer.android.com/jetpack/compose/designsystems/material3)       | Design system components                      |
|                  | [Material 3 Adaptive](https://developer.android.com/develop/ui/compose/layouts/adaptive)  | Adaptive layouts for different screen sizes   |
|                  | [Accompanist](https://google.github.io/accompanist/)                                      | Compose utility libraries                     |
|                  | [Coil](https://coil-kt.github.io/coil/)                                                   | Async image loading with Compose support      |
