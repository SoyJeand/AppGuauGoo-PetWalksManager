plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlinKapt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.example.appguaugo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.appguaugo"
        minSdk = 28
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
        viewBinding = true
    }


    secrets {
            // Optionally specify a different file name containing your secrets.
            // The plugin defaults to "local.properties"
            propertiesFileName = "secrets.properties"

            // A properties file containing default secret values. This file can be
            // checked in version control.
            defaultPropertiesFileName = "local.defaults.properties"

            // Configure which keys should be ignored by the plugin by providing regular expressions.
            // "sdk.dir" is ignored by default.
            ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
            ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
    }

        /*
        composeOptions {
            kotlinCompilerExtensionVersion = "1.5.8"
        }*/
    /*packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }*/
}

dependencies {
    implementation(libs.play.services.maps)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.compose.foundation)
    val room_version = "2.6.1"

    // Librería para convertir objetos Kotlin a y desde JSON (Necesario para el MapConverter de Room)
    implementation("com.google.code.gson:gson:2.10.1") // O la versión más reciente
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    //Material-icons-extended
    implementation("androidx.compose.material:material-icons-extended")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // implementation(libs.maps.compose) // Si usas el catálogo de versiones (TOML)
    implementation("com.google.maps.android:maps-compose:4.3.3") // Revisa la última versión

    // Para la navegación entre pantallas
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Para cargar imágenes desde internet (si lo necesitas)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Maps SDK for Android
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    // Si usas catálogo de versiones (libs.versions.toml) implementation(libs.accompanist.permissions)
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    //Play Servicies Location
    implementation("com.google.android.gms:play-services-location:21.2.0") // <--- AÑADE ESTA LÍNEA

    // En build.gradle.kts (Module :app)
    implementation("com.google.android.libraries.places:places:3.4.0") // O la última versión

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0") // O la versión más reciente que sea compatible










}