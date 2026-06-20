plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "monitoringcamera.transmitterconnect.officeconnectcamera"
    compileSdk = 35

    defaultConfig {
        applicationId = "monitoringcamera.transmitterconnect.officeconnectcamera"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                arguments("-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON")
            }
        }
        manifestPlaceholders["izooto_app_id"] = "08318911261f48444af2588779ef790c614ce7d4"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "4.1.0"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")

    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    implementation("com.airbnb.android:lottie-compose:6.3.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")
    implementation("com.github.alexeyvasilyev:rtsp-client-android:5.6.3")
    implementation(libs.coil.compose)
    implementation(libs.libphonenumber)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)
    implementation(libs.play.services.auth)
    implementation(libs.socket.io)
    implementation(libs.webrtc)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    implementation("com.facebook.android:audience-network-sdk:6.21.0")
    implementation("com.google.android.gms:play-services-ads:23.3.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.onesignal:OneSignal:4.8.5")
    implementation("com.github.bumptech.glide:glide:4.13.2")
    implementation("com.izooto:android-sdk:3.3.2")
    implementation(libs.androidx.lifecycle.process)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}