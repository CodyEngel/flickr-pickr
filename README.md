# flickr-pickr
A simple flickr client for Android.

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
| **Testing**      | [JUnit](https://junit.org/junit4/)                                                        | Unit test runner                              |
|                  | [Coroutines Test](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/) | Testing coroutines and flows                  |
|                  | [MockK](https://mockk.io/)                                                                | Kotlin-first mocking                          |
|                  | [Strikt](https://strikt.io/)                                                              | Fluent assertion library                      |
| **UI**           | [Jetpack Compose](https://developer.android.com/jetpack/compose)                          | Declarative UI framework                      |
|                  | [Material 3](https://developer.android.com/jetpack/compose/designsystems/material3)       | Design system components                      |
|                  | [Coil](https://coil-kt.github.io/coil/)                                                   | Async image loading with Compose support      |
