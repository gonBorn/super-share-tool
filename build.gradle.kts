val ktorVersion = "2.3.12"

plugins {
  kotlin("jvm") version "2.1.20"
  id("org.jlleitschuh.gradle.ktlint") version "12.3.0"
  application
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.github.gonborn"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation("io.arrow-kt:arrow-core:2.1.0")
  implementation("io.arrow-kt:arrow-fx-coroutines:2.1.0")

  implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
  implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
  implementation("io.ktor:ktor-server-html-builder-jvm:$ktorVersion")
  implementation("org.slf4j:slf4j-simple:2.0.13")
  implementation("net.lingala.zip4j:zip4j:2.11.5")

  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}
kotlin {
  jvmToolchain(21)
}

application {
  mainClass.set("MainKt")
}

tasks.shadowJar {
    archiveBaseName.set("super-share")
    archiveClassifier.set("")
    archiveVersion.set("1.0.0")
}
