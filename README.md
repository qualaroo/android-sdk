# Qualaroo Mobile SDK for Android - sources
## Quick setup
Clone repo. Make sure that `gradlew` file is executable.
It is best to open project with either Android Studio or IntelliJ IDE.
## Project modules
**demo-app** - Android application module with simple example on how to use Qualaroo SDK. Used mostly for testing.
**qualaroo-sdk** - Android library module containing the SDK.
## Simple Publish Guide
Set `QUALAROO_S3_ACCESS_KEY` and `QUALAROO_S3_SECRET_KEY` in your gradle.properties file.
Build the project with:
`./gradlew qualaroo-sdk:build`
Deploy to S3 with:
`./gradlew publish`

Make sure to set a correct version in `qualaro-sdk/build.gradle` file (publishing->publications->aar->version).
SNAPSHOT suffix is only for publishing nightly builds.
