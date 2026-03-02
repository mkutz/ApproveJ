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
    id = "org.approvej.intellij"
    name = "ApproveJ"
    version = project.version.toString()
    description =
      "Approval testing support for ApproveJ: diff viewer and one-click approval of .received files."
    vendor { name = "ApproveJ" }
    ideaVersion { sinceBuild = "251" }
  }
}

/* Use JUnit 5.11 for IntelliJ platform tests to avoid version conflicts with IntelliJ's test
framework */
val junitVersion = "5.11.3"
val junitPlatformVersion = "1.11.3"

dependencies {
  intellijPlatform {
    intellijIdeaCommunity(libs.versions.intellij.ide)
    bundledPlugin("com.intellij.java")
    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.Plugin.Java)
  }

  testImplementation(libs.assertj.core)
  testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
  testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
  testImplementation("org.opentest4j:opentest4j:1.3.0")
  testImplementation("junit:junit:4.13.2")

  testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
  testRuntimeOnly("org.junit.vintage:junit-vintage-engine:$junitVersion")
}

// Force JUnit 5.11 versions over JUnit 6 from BOM
configurations.testRuntimeClasspath {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.junit.platform") useVersion(junitPlatformVersion)
    if (requested.group == "org.junit.jupiter") useVersion(junitVersion)
    if (requested.group == "org.junit.vintage") useVersion(junitVersion)
  }
}

configurations.testCompileClasspath {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.junit.platform") useVersion(junitPlatformVersion)
    if (requested.group == "org.junit.jupiter") useVersion(junitVersion)
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
