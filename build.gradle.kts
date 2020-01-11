import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.61"
    kotlin("kapt") version "1.3.61"
}

version = "0.1-DEV"

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib"))
    compile("com.squareup.okhttp3:okhttp:3.14.2")
    compile("com.github.ajalt:clikt:2.1.0")

    compile("com.squareup.moshi:moshi:1.9.2")
    compile("com.squareup.moshi:moshi-kotlin:1.9.2")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.9.2")

    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}