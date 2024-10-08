# AI Dialogue Assistant

This is an Android application developed in Kotlin. It allows users to engage in a conversation with an AI in 108 multiple languages. The application uses Amazon Polly and Google Text to Speech for text-to-speech conversion and supports 41 languages for text to speech. It uses Jetpack Compose for the UI and Retrofit for network calls.

## Features

- **Language Support:** Supports 108 languages for conversations with Gemini API.
- **Text-to-Speech:** Uses Amazon Polly and Google TTS for text-to-speech conversion and ExoPlayer for Playback. Supports 41 languages
- **Speech Recognition:** Uses Android Speech Recognizer to detect speech for conversing with AI.
- **Backend AWS Service:** Uses AWS as the backend. AWS Lambda function to convert the text to speech(for amazon polly) and store the generated MP3 file in an S3 bucket. The Lambda function creates a temporary URL to access the MP3 file from the S3 bucket. For Google TTS, we use AWS lambda function to make the calls to the Google API which returns a string of audiocontent. This is then returned and converted to an audioURI
- **Caching:** Uses an LruCache to cache audio, reducing the need for repeated calls to the backend service(AWS).
- **Firebase:** Used for user Authentication
- **MongoDB:** Used to store the users chat and retrieve it so user can continue conversations from where they left off. 

## Developers

- Get an API key from Gemini, which would replace BuildConfig.GEMINI_KEY. Get an API key from Google TTS, make a lambda function in AWS to call Google TTS, connect an AWS API gateway to your lambda function and that should replace BuildConfig.API_GOOGLETTS.
- Also write a lambda function for Amazon Polly. This lamda function should generate the audio file and store the file in an S3 bucket. Connect this all to an API gateway and that should replace BuildConfig.API_POLLY
- Set up Firebase and your backend
