import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.*

description = "rPi Projects :: Pool"

buildscript {

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        classpath("com.github.ben-manes:gradle-versions-plugin:${project.extra["gradleVersionsPluginVersion"]}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.extra["kotlinVersion"]}")
        classpath("org.jetbrains.kotlin:kotlin-allopen:${project.extra["kotlinVersion"]}")
        classpath("org.jetbrains.kotlin:kotlin-noarg:${project.extra["kotlinVersion"]}")
        classpath("org.junit.platform:junit-platform-gradle-plugin:${project.extra["junitPlatformVersion"]}")
    }
}

plugins {
    base
    java
    application
    kotlin("jvm") version "1.8.10"
}

apply {
    plugin("idea")
    plugin("kotlin-spring")
    plugin("kotlin-noarg")
    plugin("com.github.ben-manes.versions")
}

application {
    mainClassName = "org.rpi.projects.pool.starter.RpiPoolStarterKt"
    applicationName = "rpi-pool"
}

distributions {
    getByName("main") {
        //baseName = "rpi-pool"
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    //Kotlin addons
    implementation(kotlin("stdlib", "${project.extra["kotlinVersion"]}"))
    implementation(kotlin("reflect", "${project.extra["kotlinVersion"]}"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")

    //Pi4J
    implementation("com.pi4j:pi4j-core:${project.extra["pi4jVersion"]}")
    implementation("com.pi4j:pi4j-gpio-extension:${project.extra["pi4jGpioVersion"]}")
    implementation("com.pi4j:pi4j-device:${project.extra["pi4jGpioVersion"]}")

    //Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-webflux:${project.extra["springBootVersion"]}")
    implementation("org.springframework.boot:spring-boot-starter-actuator:${project.extra["springBootVersion"]}")
    implementation("org.springframework.boot:spring-boot-starter-security:${project.extra["springBootVersion"]}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${project.extra["jacksonVersion"]}")
    // https://mvnrepository.com/artifact/javax.xml.bind/jaxb-api
    implementation ("javax.xml.bind:jaxb-api:2.3.1")


    implementation("io.jsonwebtoken:jjwt:${project.extra["jwtVersion"]}")
    implementation("io.github.microutils:kotlin-logging:${project.extra["kotlinLoggingVersion"]}")

    testImplementation("org.springframework.boot:spring-boot-starter-test:${project.extra["springBootVersion"]}") {
        exclude(module = "junit")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:${project.extra["junitVersion"]}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${project.extra["junitVersion"]}")
    testImplementation(kotlin("test"))
}

tasks {
    test {
        useJUnitPlatform()
    }
    withType<Jar> {
        manifest.attributes(
            mapOf(
                "Created-By" to "Gradle " + gradle.gradleVersion,
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Build-Time" to SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date())
            )
        )
    }
    withType<Wrapper> {
        gradleVersion = "${project.extra["gradleWrapperVersion"]}"
    }
    withType<KotlinCompile>().all {
        kotlinOptions {
            jvmTarget = "${project.extra["javaVersion"]}"
        }
    }
    getByName<Tar>("distTar").enabled = false
    getByName<Zip>("distZip").archiveName = "${project.name}.zip"
}