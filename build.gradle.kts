group = "mikaojk.github.io"
version = "0.0.1"

val javaVersion = 25

val logbackVersion= "1.5.23"
val logstashEncoderVersion = "9.0"
val poiVersion = "5.5.1"
val twilioVersion = "11.2.0"
val junitJupiterVersion = "6.0.1"


plugins {
    id("application")
    kotlin("jvm") version "2.3.0"
}


application {
    mainClass.set("mikaojk.github.io.ApplicationKt")
}

kotlin {
    jvmToolchain(javaVersion)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.poi:poi-ooxml:$poiVersion")
    implementation("com.twilio.sdk:twilio:$twilioVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


tasks {

    withType<Test> {
        useJUnitPlatform {}
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

}
