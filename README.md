Capture the Flag
================

Capture the Flag is a sample application demonstrating the use of Nokia X
services on Nokia X software platform: HERE Maps, Nokia Notifications and Nokia
In-App Payment. The application also implements the corresponding Google APIs
and utilises them when the same APK file is installed on a Google services
enabled phone. The detection of services is done run-time. OpenIAB is used to
implement the in-app payment feature.

The app itself is based on the traditional outdoor game where two teams each
have their own flags, which the opposing team then tries to capture. The flags
in the game are randomly placed on the map within a fixed distance of the
player creating the game. The team who manages to get to the flag of the
opposing team first wins.

![Main menu](https://raw.github.com/nokia-developer/capture-the-flag/master/doc/screenshots/ctf_screenshot_main_view_small.png)&nbsp;
![Offline game ongoing](https://raw.github.com/nokia-developer/capture-the-flag/master/doc/screenshots/ctf_screenshot_game_play_3_small.png)

This demo application is hosted in GitHub
(https://github.com/nokia-developer/capture-the-flag) where you can find the
source code and documentation of the latest release.

Visit the project wiki for documentation:
https://github.com/nokia-developer/capture-the-flag/wiki

Offline documentation is provided with this project in `doc` folder.


How to set up and build the project
-------------------------------------------------------------------------------

**Dependencies**

The example is dependent on the following libraries, which are provided with
the project:

* *AndroidAsync* is a low level network protocol library. The project is
  hosted in GitHub: https://github.com/koush/AndroidAsync
* *HERE API library* (`com.here.android.sdk.jar`)
* *Nokia Notifications helper library* (`push.jar`)
* *OpenIAB* provides cross-platform in-app payment functionality and
  encapsulates both Nokia In-App Payment API and Google In-App Billing API.
  The project is hosted in GitHub: https://github.com/onepf/OpenIAB

In order to build the project you will also need Google Play services library.
The library can be installed via Android SDK Manager (*Extras* -> *Google Play
services*).

**API keys and IDs**

* HERE Maps
    * *Application ID* and *application token* required
    * Default ID and token provided for this sample (not guaranteed to work)
    * See http://developer.nokia.com/resources/library/nokia-x/here-maps/here-api-prerequisites.html
* Nokia Notifications
    * Sender ID required for registering to service
    * API key required for sending push notifications (server side)
    * A default ID provided for this sample
    * See http://developer.nokia.com/resources/library/nokia-x/nokia-notifications/nokia-notifications-developer-guide.html
* Nokia In-App Payment/Google In-App Billing
    * Nokia In-App Payment does not require a key, but since OpenIAB is used, a
      public is needed (because of the Google dependency)
    * See https://github.com/onepf/OpenIAB/blob/master/README.md
    * For testing purposes, you may try the key provided with
      [Trivial Drive sample](https://github.com/onepf/OpenIAB/blob/master/samples/trivialdrive/src/org/onepf/trivialdrive/MainActivity.java#L172)
    * The product IDs used are test IDs
* Google Maps: See https://developers.google.com/maps/documentation/android/start
    * A default key provided with the project, is not guaranteed to work
* Google Cloud Messaging: See https://support.google.com/googleplay/android-developer/answer/2663268
    
**Instructions for Eclipse**

1. Import the project in Eclipse (*File* -> *Import...*)
2. Import Google Play services library project (*File -> *Import...*)
    * The library is located by default in
      `<Android SDK path>/extras/google/google_play_services/libproject` folder,
      when installed with the Android SDK Manager
    * Make sure *Is Library* check box is checked in Google Play services
      project properties (*Properties* -> *Android*)
3. Add reference to Google Play services SDK
    * Open Capture the Flag project properties
    * In project properties window, select *Android* from the left hand side
    * Click *Add* button in *Library* section
    * Select Google Play services library and click *OK* 
    * For more details, see http://developer.android.com/google/play-services/setup.html
4. Check that the references are properly setup in *Java Build Path* (see the
   following figures)
    * Note that it is especially important that HERE Maps library is not
      exported! Otherwise, the app will crash.

    ![*Project properties: Libraries*](https://raw.githubusercontent.com/nokia-developer/capture-the-flag/master/doc/figures/eclipse_project_properties_1.png)

    ![*Project properties: Order and Export*](https://raw.githubusercontent.com/nokia-developer/capture-the-flag/master/doc/figures/eclipse_project_properties_2.png)

5. To run the app, select *Run* -> *Run As* -> *Android Application*

**Importing the project in IntelliJ IDEA**

1. Launch the IDE (with projects closed) and select *Import Project*
2. Browse to the root of the project and select `settings.gradle` file
3. Select option *Use default gradle wrapper (recommended)* and click *OK*

**Instructions for Gradle**

1. Open terminal/command prompt
2. Navigate to the root of the project (not to the root of the client app, but
   the entire project)
3. Clean, build and deploy
    * In Linux/Mac OS, run command: `./gradlew clean installDebug`
    * In Windows, run command: `gradlew clean installDebug`

Game server
-------------------------------------------------------------------------------

To play the online version of the game, a server is needed. The server software
is provided with the project, see
[`/server`](https://github.com/nokia-developer/capture-the-flag/tree/master/capture-the-flag-server).
The software is implemented utilising node.js and socket.io library. To test
the server, you can use free server services such as
[Heroku](https://www.heroku.com/) or [Cloud9](https://c9.io/). For more
information see the
[README](https://github.com/nokia-developer/capture-the-flag/blob/master/capture-the-flag-server/README.md)
for the server software and the
[project wiki](https://github.com/nokia-developer/capture-the-flag/wiki).


License
-------------------------------------------------------------------------------

See the license text file provided with the project. The license is also
available online at
https://github.com/nokia-developer/capture-the-flag/blob/master/License.txt


Related documentation
-------------------------------------------------------------------------------

* [Nokia X Developer's Library](http://developer.nokia.com/resources/library/nokia-x)
    * [Nokia In-App Payment](http://developer.nokia.com/resources/library/nokia-x/nokia-in-app-payment.html)
    * [Nokia Notifications](http://developer.nokia.com/resources/library/nokia-x/nokia-notifications.html)
    * [HERE Maps](http://developer.nokia.com/resources/library/nokia-x/here-maps.html)


Version history
-------------------------------------------------------------------------------

* Version 2.0: One APK solution update - Maps/location, notifications and in-app
  payment services selected run-time based on which services are available on
  the phone (either Nokia X or Google services).
* Version 1.0: The initial release.
