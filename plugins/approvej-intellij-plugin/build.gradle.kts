import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
  java
  id("org.jetbrains.intellij.platform") version "2.5.0"
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

repositories {
  mavenCentral()
  intellijPlatform { defaultRepositories() }
}

intellijPlatform {
  pluginConfiguration {
    id = "org.approvej.intellij"
    name = "ApproveJ"
    version = project.version.toString()
    description =
      "Approval testing support for ApproveJ: diff viewer and one-click approval of .received files."
    vendor { name = "ApproveJ" }
    ideaVersion { sinceBuild = "251" }
  }
}

dependencies {
  intellijPlatform {
    intellijIdeaCommunity("2025.1")
    testFramework(TestFrameworkType.Platform)
  }

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.junit.jupiter.params)
  testImplementation(libs.assertj.core)
  testImplementation("org.opentest4j:opentest4j:1.3.0")

  testRuntimeOnly(libs.junit.platform.launcher)
  testRuntimeOnly(libs.junit.jupiter.engine)
  testRuntimeOnly("junit:junit:4.13.2")
}

tasks.test { useJUnitPlatform() }
