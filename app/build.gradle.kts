plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Plugin do Firebase
}

android {
    namespace = "com.carsaimz.web"
    compileSdk = 33  // Atualizado para 33

    defaultConfig {
        applicationId = "com.carsaimz.web"
        minSdk = 21
        targetSdk = 33  // Atualizado para 33
        versionCode = 1
        versionName = "1.0.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }
signingConfigs {
        release {
            if (System.getenv("KEYSTORE_FILE") != null) {
                storeFile     file(System.getenv("KEYSTORE_FILE"))
                storePassword System.getenv("KEYSTORE_PASSWORD")
                keyAlias      System.getenv("KEY_ALIAS")
                keyPassword   System.getenv("KEY_PASSWORD")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        debug {
            applicationIdSuffix ".debug"
            debuggable true
            buildConfigField "boolean", "ENABLE_LOGS", "true"
        }
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            buildConfigField "boolean", "ENABLE_LOGS", "false"
            signingConfig signingConfigs.release
        }
    }


    buildFeatures {
        viewBinding = true
        buildConfig = true  // Adicionado para gerar BuildConfig
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.webkit:webkit:1.6.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.navigation:navigation-fragment:2.5.3")
    implementation("androidx.lifecycle:lifecycle-livedata:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.5.1")
   dependencies {
    
    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // Dependências do Firebase (não é necessário especificar versão com o BOM)
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-config")
}

// Aplicar o plugin do Google Services (NECESSÁRIO para o Firebase)
apply(plugin = "com.google.gms.google-services")
}