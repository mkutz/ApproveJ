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

  // Jackson 3 as provided dependency
  compileOnly(platform(libs.jackson3.bom))
  compileOnly(libs.jackson3.databind)
  compileOnly(libs.jackson3.dataformat.yaml)
}

testing {
  suites {
    val test by
      getting(JvmTestSuite::class) {
        useJUnitJupiter()
        dependencies {
          implementation(testFixtures(project(":modules:core")))

          // Tests need Jackson
          implementation(platform(libs.jackson3.bom))
          implementation(libs.jackson3.databind)
          implementation(libs.jackson3.dataformat.yaml)

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
