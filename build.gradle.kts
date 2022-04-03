import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.benmanes.gradle.versions.updates.gradle.GradleReleaseChannel.CURRENT
import org.gradle.api.file.DuplicatesStrategy.WARN
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.6.20"
    kotlin("kapt") version "1.6.20"
    id("com.github.ben-manes.versions") version "0.42.0"
}

version = "0.1-DEV"

repositories {
    mavenCentral()
    google()
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
val kaptBench: Configuration by configurations.getting { extendsFrom(configurations.kaptTest.get()) }

dependencies {
    // Common versions
    val log4j2Version = "2.17.2"
    val daggerVersion = "2.41"
    val moshiVersion = "1.13.0"
    val okhttpVersion = "4.9.3"
    val retrofitVersion = "2.9.0"
    val jmhVersion = "1.35"

    // Dependencies
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.20") // Specify reflect library explicitly to stop duplicate classpath build warnings

    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.1.0")
    implementation("org.apache.logging.log4j:log4j-api:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")

    api("com.google.dagger:dagger:$daggerVersion")
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
    implementation("com.google.code.findbugs:jsr305:3.0.2")

    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")

    implementation("com.squareup.moshi:moshi:$moshiVersion")
    implementation("com.squareup.moshi:moshi-adapters:$moshiVersion")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")

    implementation("com.github.ajalt.clikt:clikt:3.4.0")

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.assertj:assertj-core:3.22.0")
    testImplementation("io.mockk:mockk:1.12.3")

    kaptTest("com.google.dagger:dagger-compiler:$daggerVersion")

    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
    testImplementation("io.github.classgraph:classgraph:4.8.143")

    // Bench dependencies
    benchImplementation("com.google.jimfs:jimfs:1.2")
    benchImplementation("org.openjdk.jmh:jmh-core:$jmhVersion")
    kaptBench("org.openjdk.jmh:jmh-generator-annprocess:$jmhVersion")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    register("bench", type=JavaExec::class) {
        dependsOn("benchClasses")
        group = "benchmark"
        mainClass.set("org.openjdk.jmh.Main")
        classpath = sourceSets["bench"].runtimeClasspath
        // To pass parameters ("-h" gives a list of possible parameters)
        // args(listOf("-h"))
    }
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Copy> {
    duplicatesStrategy = WARN // FIXME: This shouldn't be a thing
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
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