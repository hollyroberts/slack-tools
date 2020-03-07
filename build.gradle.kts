import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.70"
    kotlin("kapt") version "1.3.70"
}

version = "0.1-DEV"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.squareup.okhttp3:okhttp:3.14.2")
    implementation("com.github.ajalt:clikt:2.1.0")

    implementation("com.squareup.moshi:moshi:1.9.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.9.2")
    implementation("com.squareup.moshi:moshi-adapters:1.9.2")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.9.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
    testImplementation("org.assertj:assertj-core:3.15.0")
}

tasks.test {
    useJUnitPlatform()
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_12
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "12"
}