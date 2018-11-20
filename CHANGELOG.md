## 1.8.4
- support for running in multiple-process environment

## 1.8.3
- [hotfix] fixed invalid dependency on `jexl` module which was preventing building the library

## 1.8.2
- [fix] removed dependency of `commons-logging` to avoid crashes on Android Pie

## 1.8.1
- [hotfix] fixed the bytecode after removing the need for desugaring process

## 1.8.0
- support for displaying progress bars on surveys
- the SDK does no longer require desugar process to run - an app is no longer required to have a `compileOptions{}` clause to support Java 1.8 features
- the SDK won't longer crash when you provide an invalid API key - please check a device's logcat output to find out more info in case of any issus 
- couple of minor fixes for saving the state of some questions when configChanges happen

## 1.7.2
- fixed a bug with user's locale being ignored when deciding which version of survey should be displayed
- fixed a bug related to injecting custom properties

## 1.7.1
- support for binary-type questions
- support for min/max checkbox answers
- support for custom properties in questions/descriptions
- support for changing opacity of survey's background
- [experimental] support for AB testing surveys

## 1.6.3
- radio and checkbox improvements for pre-Marshmallow devices

## 1.6.2
- fixed bug which could cause crashes on older devices while using CTA messages

## 1.6.1
- fixed bug related to survey's not being properly marked as seen/finished
- fixed bug related to displaying messages

## 1.6.0
**Features:**
- support for observing surveys related events:
  - show (start)
  - dismiss
  - finish
- support for omitting survey's targeting checks

## 1.5.0
**Features:**
- support for freeform comments in radio and checkbox type questions
- support for displaying question's description
- support for new colors customization options
- support for anchoring more than one answer as the last one

**Bug fixes:**
- fixed few cases in which user's parameters might not have been reported back to Qualaroo's API
-- -- https://github.com/qualaroo/AndroidSDK/issues/7
- workaround for a bug related to D8 compiler and MediaTek processors:
-- -- https://issuetracker.google.com/issues/69364976
-- -- https://github.com/qualaroo/AndroidSDK/issues/6

## 1.4.4
**Changes**
- removed 72h time limit between showing the same survey

**Bug fixes**
- fixed a rare case in which SDK could display an incompatible survey

## 1.4.3
**Bug fixes:**
- https://github.com/qualaroo/AndroidSDK/issues/5

## 1.4.2
**New features:**
- support for displaying custom logos

**Various improvements**
- additional ProGuard rules for non-OkHttp3 apps
- preventing multiple clicks on radio buttons in answers
- differentiating active and paused surveys
- few additional log messages

## 1.4.1
- fix for https://github.com/qualaroo/AndroidSDK/issues/2

## 1.4.0
**New features:**
- support for Lead Generation Form
- support for Call To Action messages
- support for Dropdown questions
- support for targeting surveys by device type (phone/tablet)
- support for targeting surveys by user's identity (known/unknown/both)
- support for targeting surveys by percentage of users

**UI:**
- various layout and animations tweaks to provide smoother answering experience

**Bug fixes:**
- added missing `WAKE_LOCK` and `INTERNET` permissions
- minor fixes to improve stability

## 1.0.0
Initial release