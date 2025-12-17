# Simple bible group meeting sms notification app

## Technologies used
* Kotlin
* Gradle
* JDK 25


## Getting started

### Prerequisites
Make sure you have the Java JDK 21 installed
You can check which version you have installed using this command:
``` bash
java -version
```

### Running the application locally

#### Building the application
Need to set an environment variables
GOOGLE_SHEET_XLSX_URL to current google sheet
AUTH_TOKEN the token for twilio 
ACCOUNT_SID the account sid from twilio
PHONENUMBERS_TO_NOTIFY to the phonenumber you want to send to.
Here is a .bashrc file example:
``` shell bash
export GOOGLE_SHEET_XLSX_URL='https://docs.google.com/spreadsheets/d/12312454123123/export?format=xlsx#gid=0'
export AUTH_TOKEN='supersecretkey'
export ACCOUNT_SID='supersecretkey'
export PHONENUMBERS_TO_NOTIFY='47249817,47249817'
```
login to https://console.twilio.com/ to find AUTH_TOKEN and ACCOUNT_SID

To build locally
``` shell bash
./gradlew clean build
```

To run, you can simply run this
``` shell bash
./gradlew run
```
or on windows 
```
gradlew.bat run
```
