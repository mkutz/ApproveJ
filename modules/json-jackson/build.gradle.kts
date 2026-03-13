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

  compileOnly(platform(libs.jackson2.bom))
  compileOnly(libs.jackson2.databind)
  compileOnly(libs.jackson2.jsr310)
}

testing {
  suites {
    val test by
      getting(JvmTestSuite::class) {
        useJUnitJupiter()
        dependencies {
          implementation(testFixtures(project(":modules:core")))

          implementation(platform(libs.jackson2.bom))
          implementation(libs.jackson2.databind)
          implementation(libs.jackson2.jsr310)

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
