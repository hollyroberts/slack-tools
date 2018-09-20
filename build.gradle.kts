import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.2.70"
    kotlin("kapt") version "1.2.70"
}

version = "0.1-DEV"

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("com.squareup.okhttp3:okhttp:3.11.0")
    compile("com.github.ajalt:clikt:1.4.0")

    compile("com.squareup.moshi:moshi:1.6.0")
    // compile("com.squareup.moshi:moshi-kotlin:1.6.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.6.0")

    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}