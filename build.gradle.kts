import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.junit.platform.console.options.Details
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.text.RegexOption.*

description = "rPi Projects :: Pool"

buildscript {

    repositories {
        mavenLocal()
//        maven(url = "http://126.246.166.79:8081/repository/maven-public")
        mavenCentral()
        jcenter()
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
    kotlin("jvm") version "1.3.11"
}

apply {
    plugin("idea")
    plugin("kotlin-spring")
    plugin("kotlin-noarg")
    plugin("com.github.ben-manes.versions")
    plugin("org.junit.platform.gradle.plugin")
}

application {
    mainClassName = "org.rpi.projects.pool.starter.RpiPoolStarterKt"
    applicationName = "rpi-pool"
}

distributions {
    getByName("main") {
        baseName = "rpi-pool"
    }
}

repositories {
    mavenLocal()
//    maven(url = "http://126.246.166.79:8081/repository/maven-public")
    mavenCentral()
    jcenter()
    maven(url = "http://oss.jfrog.org/artifactory/oss-snapshot-local")
    maven(url = "https://oss.sonatype.org/content/groups/public")
}

dependencies {
    //Kotlin addons
    implementation(kotlin("stdlib", "${project.extra["kotlinVersion"]}"))
    implementation(kotlin("reflect", "${project.extra["kotlinVersion"]}"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.1.0")

    //Pi4J
    implementation("com.pi4j:pi4j-core:${project.extra["pi4jVersion"]}")
    implementation("com.pi4j:pi4j-gpio-extension:${project.extra["pi4jVersion"]}")
    implementation("com.pi4j:pi4j-device:${project.extra["pi4jVersion"]}")

    //Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-webflux:${project.extra["springBootVersion"]}")
    implementation("org.springframework.boot:spring-boot-starter-actuator:${project.extra["springBootVersion"]}")
    implementation("org.springframework.boot:spring-boot-starter-security:${project.extra["springBootVersion"]}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${project.extra["jacksonVersion"]}")

    implementation("io.jsonwebtoken:jjwt:${project.extra["jwtVersion"]}")
    implementation("io.github.microutils:kotlin-logging:${project.extra["kotlinLoggingVersion"]}")

    testImplementation("org.springframework.boot:spring-boot-starter-test:${project.extra["springBootVersion"]}") {
        exclude(module = "junit")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:${project.extra["junitVersion"]}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${project.extra["junitVersion"]}")
}

tasks {
    withType<Jar> {
        manifest.attributes(mapOf(
                "Created-By" to "Gradle " + gradle.gradleVersion,
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Build-Time" to SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date()))
        )
    }
    withType<Wrapper> {
        gradleVersion = "${project.extra["gradleWrapperVersion"]}"
    }
    withType<KotlinCompile>().all {
        sourceCompatibility = "${project.extra["javaVersion"]}"
        targetCompatibility = "${project.extra["javaVersion"]}"
        kotlinOptions {
            jvmTarget = "${project.extra["javaVersion"]}"
            languageVersion = "1.3"
        }
    }
    getByName<Tar>("distTar").enabled = false
    getByName<Zip>("distZip").archiveName = "${project.name}.zip"
}