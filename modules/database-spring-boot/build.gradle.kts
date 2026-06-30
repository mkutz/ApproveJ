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
  api(project(":modules:database-jdbc"))
  api(libs.jspecify)

  compileOnly(libs.spring.boot.autoconfigure)
}

testing {
  suites {
    getByName<JvmTestSuite>("test") {
      useJUnitJupiter()
      dependencies {
        implementation(libs.spring.boot.autoconfigure)
        implementation(libs.spring.boot.test)
        implementation(libs.h2)

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
