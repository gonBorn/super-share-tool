import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val ktorVersion = "2.3.12"

plugins {
  kotlin("jvm") version "2.1.20"
  id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
  id("org.jetbrains.compose") version "1.6.11"
  id("org.jlleitschuh.gradle.ktlint") version "12.3.0"
  id("com.github.johnrengelman.shadow") version "8.1.1"
  kotlin("plugin.serialization") version "2.1.20"
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
  implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
  implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
  implementation("io.ktor:ktor-utils-jvm:$ktorVersion")

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

tasks.build {
  dependsOn(tasks.shadowJar)
}

kotlin {
  jvmToolchain(21)
}

compose.desktop {
  application {
    mainClass = "MainKt"
    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Deb)
      packageName = "SuperShare"
      packageVersion = "1.0.0"
      vendor = "zeyan"

      windows {
        iconFile.set(project.file("src/main/resources/file-share.ico"))
      }
    }
  }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
  archiveBaseName.set("super-share")
  archiveClassifier.set("")

  manifest {
    attributes(
      "Main-Class" to "MainKt",
    )
  }
  from(sourceSets.main.get().output)
  from(sourceSets.main.get().resources)
}
