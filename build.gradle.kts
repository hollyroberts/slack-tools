import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.benmanes.gradle.versions.updates.gradle.GradleReleaseChannel.CURRENT
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.4.21"
    kotlin("kapt") version "1.4.21"
    id("com.github.ben-manes.versions") version "0.36.0"
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
val kaptBench: Configuration by configurations.getting { extendsFrom(configurations.kaptTest.get()) }

dependencies {
    // Common versions
    val log4j2Version = "2.14.0"
    val daggerVersion = "2.30.1"
    val assistedInjectVersion = "0.6.0"
    val moshiVersion = "1.11.0"
    val okhttpVersion = "4.9.0"
    val retrofitVersion = "2.9.0"

    // Dependencies
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.21") // Specify reflect library explicitly to stop duplicate classpath build warnings

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
    implementation("com.squareup.moshi:moshi-adapters:$moshiVersion")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")

    implementation("com.github.ajalt.clikt:clikt:3.0.1")

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation("org.assertj:assertj-core:3.18.1")
    testImplementation("io.mockk:mockk:1.10.5")

    kaptTest("com.google.dagger:dagger-compiler:$daggerVersion")

    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
    testImplementation("io.github.classgraph:classgraph:4.8.98")

    // Bench dependencies
    benchImplementation("com.google.jimfs:jimfs:1.2")
    benchImplementation("org.openjdk.jmh:jmh-core:1.27")
    kaptBench("org.openjdk.jmh:jmh-generator-annprocess:1.26")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    register("bench", type=JavaExec::class) {
        dependsOn("benchClasses")
        group = "benchmark"
        main = "org.openjdk.jmh.Main"
        classpath = sourceSets["bench"].runtimeClasspath
        // To pass parameters ("-h" gives a list of possible parameters)
        // args(listOf("-h"))
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_15
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "15"
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