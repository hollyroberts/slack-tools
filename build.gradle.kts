import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.benmanes.gradle.versions.updates.gradle.GradleReleaseChannel.CURRENT
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.4.10"
    kotlin("kapt") version "1.4.10"
    id("com.github.ben-manes.versions") version "0.30.0"
}

version = "0.1-DEV"

repositories {
    mavenCentral()
}

sourceSets {
    create("bench") {
        java.srcDir("src/bench/java")
        resources.srcDir("src/bench/resources")

        compileClasspath += main.get().output + test.get().output
        runtimeClasspath += main.get().output + test.get().output
    }
}

val benchImplementation: Configuration by configurations.getting { extendsFrom(configurations.implementation.get()) }
configurations["benchImplementation"].extendsFrom(configurations.testImplementation.get())
configurations["benchRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())
configurations["benchCompileOnly"].extendsFrom(configurations.testCompileOnly.get())

dependencies {
    // Common versions
    val log4j2Version = "2.13.3"
    val daggerVersion = "2.28.3"
    val assistedInjectVersion = "0.5.2"
    val moshiVersion = "1.10.0"
    val okhttpVersion = "4.8.1"
    val retrofitVersion = "2.9.0"

    // Dependencies
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.10")

    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.0.0")
    implementation("org.apache.logging.log4j:log4j-api:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")

    api("com.google.dagger:dagger:$daggerVersion")
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("com.squareup.inject:assisted-inject-annotations-dagger2:$assistedInjectVersion")
    kapt("com.squareup.inject:assisted-inject-processor-dagger2:$assistedInjectVersion")

    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")

    implementation("com.squareup.moshi:moshi:$moshiVersion")
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    implementation("com.squareup.moshi:moshi-adapters:$moshiVersion")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")

    implementation("com.github.ajalt:clikt:2.8.0")

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("org.assertj:assertj-core:3.17.2")
    testImplementation("io.mockk:mockk:1.10.0")

    kaptTest("com.google.dagger:dagger-compiler:$daggerVersion")

    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
    testImplementation("io.github.classgraph:classgraph:4.8.89")

    benchImplementation("com.google.jimfs:jimfs:1.1")
}

tasks.test {
    useJUnitPlatform()
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_14
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "14"
    kotlinOptions.freeCompilerArgs += "-Xjvm-default=all"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"

    kapt.includeCompileClasspath = false
}

tasks.withType<DependencyUpdatesTask> {
    checkForGradleUpdate = true
    gradleReleaseChannel = CURRENT.toString()

    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}