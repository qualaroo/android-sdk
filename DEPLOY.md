## Checkout and build
#### Regular approach
Get Android Studio 3.1 and just import the project. Gradle plugin should fetch all required dependencies for you.

#### Console approach
```
brew cask install android-sdk
//set up an ANDROID_HOME env variable pointing to the android-sdk folder
mkdir -p "$ANDROID_HOME/licenses"
echo -e "\n8933bad161af4178b1185d1a37fbf41ea5269c55" > "$ANDROID_HOME/licenses/android-sdk-license"
echo -e "\n84831b9409646a918e30573bab4c9c91346d8abd" > "$ANDROID_HOME/licenses/android-sdk-preview-license"
//from inside of the project
./gradlew clean qualaroo-sdk:build
```

## Publish
Set `QUALAROO_S3_ACCESS_KEY` and `QUALAROO_S3_SECRET_KEY` environment variables in your `gradle.properties` file. 

**Be sure to perform a full clean build before running a `publish***` task as Gradle tries to run it simultaneously with the build task and you might release a wrong version by an accident!**

There is also regular `publish` available, but it was disabled on purpose in order to prevent anyone accidentaly releasing new version to public.
#### Build
```
./gradlew clean qualaroo-sdk:build
```

#### Release to S3-Staging
```
./gradlew publishAarPublicationToQualarooSnapshotsRepository
```

#### Release to S3-Production
```
./gradlew publishAarPublicationToQualarooProductionRepository
```

#### Release to your local maven server
```
./gradlew publishAarPublicationToMavenLocal
```