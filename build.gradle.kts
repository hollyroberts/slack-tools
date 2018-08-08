@file:Suppress("PropertyName")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    var kotlin_version: String by extra
    kotlin_version = "1.2.50"

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlinModule("gradle-plugin", kotlin_version))
    }
}

version = "0.1-DEV"

apply {
    plugin("kotlin")
    plugin("kotlin-kapt")
}

val kotlin_version: String by extra

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    compile(kotlinModule("stdlib-jdk8", kotlin_version))
    compile("com.squareup.okhttp3:okhttp:3.11.0")
    compile("com.squareup.moshi:moshi:1.6.0")
    compile("com.github.ajalt:clikt:1.4.0")

    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.6.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}