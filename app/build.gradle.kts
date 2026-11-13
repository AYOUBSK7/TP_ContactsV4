import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// La clé Google Maps est lue dans local.properties (fichier ignoré par Git)
val proprietesLocales = Properties()
val fichierLocal = rootProject.file("local.properties")
if (fichierLocal.exists()) {
    fichierLocal.inputStream().use { proprietesLocales.load(it) }
}

android {
    namespace = "com.example.contactsv4"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.contactsv4"
        minSdk = 24
        // 34 : évite que l'écran passe sous la barre d'état (Android 15+)
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Remplace ${MAPS_API_KEY} dans AndroidManifest.xml
        // (valeur par défaut non vide : sans clé la carte reste grise mais l'appli ne plante pas)
        manifestPlaceholders["MAPS_API_KEY"] = proprietesLocales.getProperty("MAPS_API_KEY", "CLE_MAPS_MANQUANTE")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.play.services.maps)
}
