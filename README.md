<p align="center"><img src="img/logo-dark.png?raw=true" align="center" width="300"/></p>
<h1 align="center">QualarooSDK for Android</h1>

<p align="center">
  <a href="https://github.com/qualaroo/android-sdk/blob/dev/CHANGELOG.md">
    <img src="https://img.shields.io/badge/version-1.8.4-blue.svg">
  </a>
  <a href="https://circleci.com/gh/qualaroo/android-sdk/tree/dev">
    <img src="https://img.shields.io/circleci/project/github/qualaroo/android-sdk/dev.svg">
  </a>  
</p>

<p align="center">
  Qualaroo helps companies identify and capitalize on mobile visitor revenue opportunities.
</p>

## Requirements

In order to integrate the Qualaroo SDK into a 3rd-party app, the app must satisfy the following requirements:

- Minimum deployment target set to Android 4.0.3 or later

## Step 1. Integration

#### Using Gradle

The recommended way to install the library for Android is with build system like Gradle.

Simply add the `com.qualaroo:qualaroo-sdk:1.8.4` dependency to your app's `build.gradle` file:

```javascript
repositories {
    maven {
        url 'https://s3.amazonaws.com/qualaroo-android-sdk/libs'
    }
}
dependencies {
    implementation 'com.qualaroo:qualaroo-sdk:1.8.4'
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

#### Configure options for displaying survey
```java
//Omit targetting options
SurveyOptions options = new SurveyOptions.Builder()
            .ignoreSurveyTargeting(true)
            .build();
Qualaroo.getInstance().showSurvey("your_survey_alias", options);
```

#### Observe survey related events
In order to be able to listen to events, you need to create your own implementation of `com.qualaroo.QualarooSurveyEventReceiver` class.

In `onSurveyEvent(@NonNull String surveyAlias, @Type int eventType)` method you will be notified of events related to a particular survey. `eventType` can be one of the following:
```java 
//survey has just been shown to a user
QualarooSurveyEventReceiver.EVENT_TYPE_SHOWN 

//survey has been dismissed by a user
QualarooSurveyEventReceiver.EVENT_TYPE_DISMISSED 

//user has completed the survey by answering all of the questions
QualarooSurveyEventReceiver.EVENT_TYPE_FINISHED 
```    

To enable your receiver, register it in your `AndroidManifest.xml` file:
```xml
<application>
    ...
    <receiver 
        android:name="com.example.MySurveyEventReceiver"
        android:exported="false">
        <intent-filter>
            <action android:name="com.qualaroo.event.ACTION_SURVEY_EVENT"/>
        </intent-filter>
    </receiver>
</application> 
```

#### Run AB tests [experimental!]
You might want to test multiple surveys at once and verify which performs best.
Out of surveys provided, one will be chosen on a random basis and presented to the user.
This choice will be stored throught multiple app launches.

To run an AB test out of surveys "A", "B" and "C":
```java
Qualaroo.getInstance().abTest()
       .fromSurveys(Arrays.asList("my_survey_A", "my_survey_B", "my_survey_C"))
       .show();
```
Keep in mind that this is an experimental feature and it's implementation might change in future releases.

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

## ProGuard configuration
The SDK comes with pre-defined set of ProGuard rules, so you don't have to modify your own.

However, if you encounter any problems, please report it to us ([new issue](https://github.com/qualaroo/AndroidSDK/issues/new)) and try adding this rule to your configuration:

```
-keep public class com.qualaroo.internal.model.** {
    private *;
    <init>(...);
}
```
