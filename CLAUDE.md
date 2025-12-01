# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build the project
./gradlew build

# Run unit tests
./gradlew test

# Run a single test class
./gradlew test --tests "dev.engel.flickrpickr.ExampleUnitTest"

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean build

# Check for lint issues
./gradlew lint
```

## Architecture

This is a modern Android application using:

- **Single-module structure**: All code lives in the `app` module
- **Jetpack Compose**: Declarative UI with Material 3
- **Hilt**: Dependency injection with KSP annotation processing
- **MVVM**: ViewModels with Kotlin Coroutines/Flow for reactive state
- **Navigation Compose**: Single-activity navigation pattern
- **Retrofit + kotlinx.serialization**: Network layer with JSON parsing

## Testing Stack

- **JUnit 4** for test runner
- **MockK** for mocking (Kotlin-first)
- **Strikt** for fluent assertions
- **kotlinx-coroutines-test** for testing coroutines and flows

## Project Configuration

- Package: `dev.engel.flickrpickr`
- Min SDK: 24, Target SDK: 36
- Kotlin with JVM target 11
- Version catalog: `gradle/libs.versions.toml`