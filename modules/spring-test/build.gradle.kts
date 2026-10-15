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

  compileOnly(libs.spring.context)
  compileOnly(libs.spring.test)
}

testing {
  suites {
    getByName<JvmTestSuite>("test") {
      useJUnitJupiter()
      dependencies {
        implementation(libs.spring.test)
        implementation(libs.spring.boot.test)
        implementation(libs.mockito.core)

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
