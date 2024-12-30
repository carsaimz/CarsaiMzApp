plugins {
    id("com.android.application") version "8.0.0" apply false
    id("com.android.library") version "8.0.0" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    repositories {
        google()        // Repositório do Google
        mavenCentral()  // Repositório Maven Central
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.0")   // Plugin do Android
        classpath("com.google.gms:google-services:4.4.2")    // Plugin do Firebase
    }
}

allprojects {
    repositories {
        // Repositórios redundantes removidos aqui, pois já foram definidos acima
    }
}