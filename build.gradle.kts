import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.TestLoggerPlugin
import com.adarshr.gradle.testlogger.theme.ThemeType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension


plugins {
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
    id("com.adarshr.test-logger") version "3.0.0"
    id("org.jetbrains.kotlin.jvm") version "1.5.20"
    kotlin("plugin.serialization") version "1.5.20"
    `maven-publish`
}

val slf4jVersion = "1.7.31"

dependencies {
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    api("com.nimbusds:nimbus-jose-jwt:9.10.1")

    testImplementation("org.slf4j:slf4j-simple:$slf4jVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
    testImplementation("org.assertj:assertj-core:3.20.2")
}

tasks.test {
    useJUnitPlatform()
    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "9"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
        vendor.set(JvmVendorSpec.ADOPTOPENJDK)
    }
}
configure<KtlintExtension> {
    version.set("0.41.0")
}
repositories {
    mavenCentral()
}

group = "com.github.kimble.oidccliclient"
version = "1.0.0-SNAPSHOT"

plugins.withType<TestLoggerPlugin> {
    configure<TestLoggerExtension> {
        theme = ThemeType.MOCHA_PARALLEL
        slowThreshold = 2500
        showStackTraces = true
        showCauses = true
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}
