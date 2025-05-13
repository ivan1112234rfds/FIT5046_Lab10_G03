plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}
dependencies {
    // ... 其他依赖
    implementation("androidx.navigation:navigation-compose:2.7.0")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.maps.android:maps-compose:2.15.0")
    implementation("com.google.android.libraries.places:places:3.2.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.squareup.okhttp3:okhttp-dnsoverhttps:4.9.3")

    // 导入 Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    
    // 添加 Firebase Analytics
    implementation("com.google.firebase:firebase-analytics")
    
    // Firebase 身份验证和数据库
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    
    // Google 登录所需依赖
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    implementation("androidx.compose.material:material-icons-extended")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.google.accompanist:accompanist-appcompat-theme:0.28.0")
}
android {
    namespace = "com.example.activitymanager"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.activitymanager"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.perf.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}