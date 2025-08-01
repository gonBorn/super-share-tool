import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val ktorVersion = "2.3.12"

plugins {
  kotlin("jvm") version "2.1.20"
  id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
  id("org.jetbrains.compose") version "1.6.11"
  id("org.jlleitschuh.gradle.ktlint") version "12.3.0"
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.github.gonborn"
version = "1.0.0"

repositories {
  mavenCentral()
  maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  google()
}

dependencies {
  // Ktor
  implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
  implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
  implementation("io.ktor:ktor-server-html-builder-jvm:$ktorVersion")
  implementation("io.ktor:ktor-server-websockets-jvm:$ktorVersion")
  implementation("io.ktor:ktor-client-websockets:$ktorVersion")
  implementation("io.ktor:ktor-client-cio:$ktorVersion")

  // Other
  implementation("org.slf4j:slf4j-simple:2.0.13")
  implementation("net.lingala.zip4j:zip4j:2.11.5")
  implementation("com.google.zxing:core:3.5.3")
  implementation("com.google.zxing:javase:3.5.3")

  // Compose
  implementation(compose.desktop.currentOs)
}

tasks.test {
  useJUnitPlatform()
}

kotlin {
  jvmToolchain(21)
}

compose.desktop {
  application {
    mainClass = "MainKt"
    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "SuperShare"
      packageVersion = "1.0.0"
    }
  }
}

tasks.shadowJar {
  archiveBaseName.set("super-share")
  archiveClassifier.set("fat")
  manifest {
    attributes["Main-Class"] = "MainKt"
  }
}
