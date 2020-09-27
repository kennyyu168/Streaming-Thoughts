# Streaming Thoughts
An Android audio journaling app with cloud storage and multiple user accounts.

## Installation/Requirements
Since there current is no apk distribution, you will need Android Studio to run/test this application.

**Requirements:**
- Android Device running Android 11 (optional)
- Android Studio
- Firebase

**Installation:**
1. Open the StreamingThoughts folder on Android Studio
2. Configure your own Firebase storage: https://firebase.google.com/docs/android/setup
3. Configure rules for the audio folder in Firebase Storage: https://firebase.google.com/docs/storage & https://firebase.google.com/docs/storage/security/get-started
4. Select either a real device running Android 11 or emulated device running 11
5. Press Run to install and run the application

## Some Implementation Details
The storage was done using Firebase Storage. When the user is first presented the 
app, they have the choice to either just start using the application or sign in to
a preexisting account. Now, since this is pretty clunky, this issue is currently
being addressed. 

## Development Details
Initial bulk working period: June 2020 - July 2020
Second wave working period: Late September 2020 - present

## Known Bugs
- Sometimes upon login or sign up, there are multiple instances of the same audio clip

## Work in Progress 
- Upload to Google Play
- Overhauling UI elements
- Unifying UI color scheme
- Redo sign in flow for better user experience
