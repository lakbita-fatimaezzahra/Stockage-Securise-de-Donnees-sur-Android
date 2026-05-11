plugins {
    alias(libs.plugins.android.application)
    // Ajoutez cette ligne pour activer KSP
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
}
android {
    namespace = "com.example.securestorageapp"
    compileSdk = 35 // Version stable

    defaultConfig {
        applicationId = "com.example.securestorageapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Recommandé pour la sécurité (obfuscation)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Nettoyage : On utilise uniquement le catalogue libs
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Room avec KSP (pensez à ajouter le plugin ksp en haut)
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    // Sécurité : SQLCipher & Jetpack Security
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.sqlite:sqlite:2.4.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Cryptographie avancée
    implementation("com.google.crypto.tink:tink-android:1.13.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}