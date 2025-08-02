import com.github.gmazzo.gradle.plugins.launch4j.Launch4jTask
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val ktorVersion = "2.3.12"

plugins {
  kotlin("jvm") version "2.1.20"
  id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
  id("org.jetbrains.compose") version "1.6.11"
  id("org.jlleitschuh.gradle.ktlint") version "12.3.0"
  id("com.github.johnrengelman.shadow") version "8.1.1"
  id("com.github.gmazzo.launch4j") version "5.1.0"
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
      targetFormats(TargetFormat.Dmg, TargetFormat.Deb) // Keep DMG for macOS, remove MSI for Windows
      packageName = "SuperShare"
      packageVersion = "1.0.0"
    }
  }
}

val unzipAndCopyJre by tasks.registering(Copy::class) {
  val zipFile = file("assets/jdk-21.0.8+9-jre.zip")
  val outputDir =
    layout
      .buildDirectory
      .dir("launch4j/jdk-21.0.8+9-jre")
      .get()
      .asFile

  // unzip using Gradle's built-in zipTree
  from(zipTree(zipFile))
  into(outputDir)
}

tasks.withType<Launch4jTask> {
  dependsOn(unzipAndCopyJre)

  mainClassName.set("MainKt")
  from(tasks.shadowJar.flatMap { it.archiveFile })
  outputFile.set(layout.buildDirectory.file("launch4j/dist/SuperShare.exe"))
  icon.set(project.file("assets/file-share.ico"))
  headerType.set(edu.gmazzo.gradle.plugins.launch4j.dsl.HeaderType.GUI)
  jre.path.set(unzipAndCopyJre.map { it.destinationDir })
  jre.minVersion.set("21")
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
  archiveBaseName.set("super-share")
  archiveClassifier.set("")

  manifest {
    attributes(
      "Main-Class" to "MainKt",
    )
  }
}
