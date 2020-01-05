# Jenkins SCM HTTP Recorder Plugin

## Introduction

This is a Jenkins Plugin, that posts all SCM changes over HTTP as JSON to the entered URL.

## Parameters

- *url* - (required) where the JSON is send to
- *restart* - (optional) set if the project should restart the server
- *serverStage - (optional) Possible Values: DEV, TEST, WEB

## Example JSON Request
```json
{
  "buildNumber": BUILD_NUMBER,
  "commitInfos": [],
  "projectName": "PROJECT_NAME",
  "restartServer": true,
  "serverStage": "DEV",
  "timestamp": 1578147816824
}
```

## Usage in Scripted Pipeline

```groovy
node {

        git credentialsId: 'CREDENTIALS_HASH', url: 'GIT_URL'

        scmHttpRecorder(restart: true, serverStage: 'DEV', url: 'https://localhost:8080/test')

}
```