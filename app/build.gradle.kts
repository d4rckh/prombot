import com.adarshr.gradle.testlogger.theme.ThemeType;

plugins {
    application
    id("org.graalvm.buildtools.native") version "0.10.6"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.diffplug.spotless") version "7.1.0"
    id("com.adarshr.test-logger") version "4.0.0"
}

repositories {
    mavenCentral()
}

spotless {
    java {
        palantirJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

testlogger {
    theme = ThemeType.STANDARD
    showExceptions = true
    showStackTraces = true
    showFullStackTraces = true
    showCauses = true
    slowThreshold = 2000
    showSummary = true
    showSimpleNames = false
    showPassed = true
    showSkipped = true
    showFailed = true
    showOnlySlow = false
    showStandardStreams = false
    showPassedStandardStreams = true
    showSkippedStandardStreams = true
    showFailedStandardStreams = true
    logLevel = LogLevel.LIFECYCLE
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    compileOnly("org.projectlombok:lombok:1.18.38")

    implementation("org.knowm.xchart:xchart:3.8.8")
    implementation("org.java-websocket:Java-WebSocket:1.6.0")
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.19.1")
    implementation("com.google.inject:guice:7.0.0")
    implementation("com.squareup.okhttp3:okhttp:5.1.0")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("net.dv8tion:JDA:5.6.1")
    implementation(libs.guava)

    runtimeOnly("ch.qos.logback:logback-classic:1.5.18")

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("prombot")
            mainClass.set("org.prombot.App")
        }
    }
}

application {
    mainClass = "org.prombot.App"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.prombot.App"
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
