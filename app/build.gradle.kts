plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    alias(libs.plugins.googleGmsGoogleServices)
}

android {
    namespace = "com.example.ai_dialogue_assistant"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ai_dialogue_assistant"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/AL2.0"
            excludes += "/META-INF/LGPL2.1"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "mozilla/public-suffix-list.txt"
        }
    }
}


dependencies {
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // Compose viewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Compose LiveData
    implementation("androidx.compose.runtime:runtime-livedata:1.5.4")

    // Voyager
    implementation("cafe.adriel.voyager:voyager-navigator:1.0.0")
    implementation("cafe.adriel.voyager:voyager-tab-navigator:1.0.0")
    implementation("cafe.adriel.voyager:voyager-transitions:1.0.0")

    // Fuzzy algo
    implementation("me.xdrop:fuzzywuzzy:1.3.1")

    // Gemini dependency
    implementation("com.google.ai.client.generativeai:generativeai:0.2.2")

    // Coroutine
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")

    // ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")

    // Google Cloud Text-to-Speech
    implementation("com.google.cloud:google-cloud-texttospeech:1.3.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:0.26.0")
    implementation("io.grpc:grpc-okhttp:1.38.1")
    implementation("io.grpc:grpc-stub:1.38.1")
    implementation("com.google.api:gax:1.58.0")
    implementation("io.grpc:grpc-protobuf:1.38.1")
    implementation("com.google.protobuf:protobuf-java:3.17.3")
    implementation("com.google.api.grpc:proto-google-common-protos:2.5.1")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // firebase shizz
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.firebase.auth)
    // retrofit stuff
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:3.9.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
