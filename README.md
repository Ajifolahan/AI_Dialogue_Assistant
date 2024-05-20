# AI Dialogue Assistant

This is an Android application developed in Kotlin. It allows users to engage in a conversation with an AI in 134 multiple languages. The application uses Amazon Polly for text-to-speech conversion and supports 22 languages for text to speech. It uses Jetpack Compose for the UI and Retrofit for network calls.

## Features

- **Language Support:** Supports 134 languages for conversations with Gemini API.
- **Text-to-Speech:** Uses Amazon Polly for text-to-speech conversion and ExoPlayer for Playback. Supports 22 languages
- **Speech Recognition:** Uses Android Speech Recognizer to detect speech for conversing with AI.
- **Backend AWS Service:** Uses AWS as the backend. AWS Lambda function to convert the text to speech and store the generated MP3 file in an S3 bucket. The Lambda function creates a temporary URL to access the MP3 file from the S3 bucket. 
- **Caching:** Uses an LruCache to cache audio, reducing the need for repeated calls to the backend service(AWS).

