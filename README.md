[![](img/logo-dark.png?raw=true)](https://qualaroo.com/)

# QualarooSDK for Android

Qualaroo helps companies identify and capitalize on mobile visitor revenue opportunities.

## Requirements

In order to integrate the Qualaroo SDK into a 3rd-party app, the app must satisfy the following requirements:

- Minimum deployment target set to Android 4.0.3 or later

## Step 1. Integration

#### Using Gradle

The recommended way to install the library for Android is with build system like Gradle.

Simply add the `com.qualaroo:qualaroo-sdk:1.4.2` dependency to your app's `build.gradle` file:

```javascript
repositories {
    maven {
        url 'https://s3.amazonaws.com/qualaroo-android-sdk/libs'
    }
}
dependencies {
    compile 'com.qualaroo:qualaroo-sdk:1.4.2'
}
```
## Step 2. Code Integration
#### Initialize the Client
In order to be able to use Qualaroo SDK you need to to initialize it first.
This procedure needs to be done only once and because of this, we recommended initializing the SDK in your `Application` subclass.
```java
public class MyApp extends Application {
  @Override public void onCreate() {
    // Initialize the Qualaroo client with the given API Key.
    Qualaroo.initializeWith(this)
            .setApiKey("<your_api_key>")
            .init();
  }
}        
```
After initialization, the SDK will be accessible via `Qualaroo.getInstance()` method.
#### Display survey with a given alias.
The survey will be displayed if all conditions configured in our dashboard are met
```java
//Show survey with "your_survey_alias" alias
Qualaroo.getInstance().showSurvey("your_survey_alias");
```
#### Set user properties
```java
//Set unique user id
Qualaroo.getInstance().setUserId("HAL_9000");
//Set user property "name" to "Hal"
Qualaroo.getInstance().setUserProperty("name", "Hal");
//remove property "name"
Qualaroo.getInstance().removeProperty("name");
```

#### Set preferred language
You can set preferred language that you want to use when displaying surveys.
```java
//Set preferred display language to French
Qualaroo.getInstance().setPrefferedLanguage("fr");
```
Language that you provide should be an ISO 639-1 compatible language code (two lowercase letters)
## Debugging
If you run into any issues while using the Android library, we recommend turning on logging to help you trace the issue. 
You can do this by setting debug mode to true.
```java
Qualaroo.initializeWith(context)
        .setApiKey("<your_api_key>")
        .setDebugMode(true)
        .init();
```
You can find more info at our [wiki pages](https://github.com/qualaroo/AndroidSDK/wiki)
## License

Copyright (c) 2017 Qualaroo
