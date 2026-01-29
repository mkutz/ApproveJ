@file:Suppress("UnstableApiUsage", "unused")

plugins {
  `java-library`
  jacoco
  `jvm-test-suite`
  `maven-publish`
}

java {
  withJavadocJar()
  withSourcesJar()
  toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

repositories { mavenCentral() }

dependencies {
  api(project(":modules:core"))
  api(libs.jspecify)

  compileOnly(libs.jackson.databind)
  compileOnly(libs.jackson.dataformat.yaml)
  compileOnly(libs.jackson.jsr310)
}

testing {
  suites {
    val test by
      getting(JvmTestSuite::class) {
        useJUnitJupiter()
        dependencies {
          implementation(libs.jackson.databind)
          implementation(libs.jackson.dataformat.yaml)
          implementation(libs.jackson.jsr310)

          implementation(platform(libs.junit.bom))
          implementation(libs.junit.jupiter.api)
          implementation(libs.junit.jupiter.params)
          implementation(libs.assertj.core)

          runtimeOnly(libs.junit.platform.launcher)
          runtimeOnly(libs.junit.jupiter.engine)
        }
      }
  }
}

tasks.jacocoTestReport { reports { xml.required = true } }
