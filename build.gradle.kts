import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.71"
    kotlin("kapt") version "1.3.71"
}

version = "0.1-DEV"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    // TODO define versions of common libs
    api("com.google.dagger:dagger:2.27")
    kapt("com.google.dagger:dagger-compiler:2.27")
    implementation("com.google.code.findbugs:jsr305:3.0.2")

    implementation("com.google.inject:guice:4.2.3")

    implementation("com.squareup.okhttp3:okhttp:4.5.0-RC1")
    implementation("com.github.ajalt:clikt:2.1.0")

    implementation("com.squareup.moshi:moshi:1.9.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.9.2")
    implementation("com.squareup.moshi:moshi-adapters:1.9.2")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.9.2")

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
    testImplementation("org.assertj:assertj-core:3.15.0")
    testImplementation("io.mockk:mockk:1.9.3")

    kaptTest("com.google.dagger:dagger-compiler:2.27")
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