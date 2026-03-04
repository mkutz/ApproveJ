@file:Suppress("UnstableApiUsage")

import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
  java
  jacoco
  `jvm-test-suite`
  alias(libs.plugins.intellij.platform)
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

repositories {
  mavenCentral()
  intellijPlatform { defaultRepositories() }
}

intellijPlatform {
  pluginConfiguration {
    id = "org.approvej"
    name = "ApproveJ"
    version = project.version.toString()
    description =
      "Approval testing support for ApproveJ: diff viewer and one-click approval of .received files."
    vendor { name = "ApproveJ" }
    ideaVersion { sinceBuild = "251" }
  }
  publishing { token = providers.environmentVariable("INTELLIJ_MARKETPLACE_TOKEN") }
}

/* Use JUnit 5.11 for IntelliJ platform tests to avoid version conflicts with IntelliJ's test
framework */
dependencies {
  intellijPlatform {
    intellijIdeaCommunity(libs.versions.intellij.ide)
    bundledPlugin("com.intellij.java")
    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.Plugin.Java)
  }

  testImplementation(libs.assertj.core)
  testImplementation(libs.junit5.jupiter.api)
  testImplementation(libs.junit5.jupiter.params)
  testImplementation(libs.opentest4j)
  testImplementation(libs.junit4)

  testRuntimeOnly(libs.junit5.platform.launcher)
  testRuntimeOnly(libs.junit5.jupiter.engine)
  testRuntimeOnly(libs.junit5.vintage.engine)
}

// Force JUnit 5.11 versions over JUnit 6 from BOM
val junit5Version = libs.versions.junit5.get()
val junit5PlatformVersion = libs.versions.junitPlatform5.get()

configurations.testRuntimeClasspath {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.junit.platform") useVersion(junit5PlatformVersion)
    if (requested.group == "org.junit.jupiter") useVersion(junit5Version)
    if (requested.group == "org.junit.vintage") useVersion(junit5Version)
  }
}

configurations.testCompileClasspath {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.junit.platform") useVersion(junit5PlatformVersion)
    if (requested.group == "org.junit.jupiter") useVersion(junit5Version)
  }
}

tasks.test { useJUnitPlatform() }

testing {
  suites {
    val unitTest by
      registering(JvmTestSuite::class) {
        useJUnitJupiter()
        dependencies {
          implementation(platform(libs.junit.bom))
          implementation(libs.junit.jupiter.api)
          implementation(libs.assertj.core)
          implementation(project())

          runtimeOnly(libs.junit.platform.launcher)
          runtimeOnly(libs.junit.jupiter.engine)
        }
      }
  }
}

// Add IntelliJ platform JARs to unitTest so production classes can be compiled against and loaded
tasks.named<JavaCompile>("compileUnitTestJava") {
  classpath += configurations.getByName("intellijPlatformClasspath")
}

tasks.named<Test>("unitTest") { classpath += configurations.getByName("intellijPlatformClasspath") }

tasks.named("check") { dependsOn(testing.suites.named("unitTest")) }

tasks.jacocoTestReport {
  mustRunAfter(tasks.check, tasks.javadoc)
  executionData(fileTree(project.layout.buildDirectory) { include("**/jacoco/*.exec") })
  reports { xml.required = true }
}
