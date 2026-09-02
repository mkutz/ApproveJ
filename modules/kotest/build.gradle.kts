@file:Suppress("UnstableApiUsage", "unused")

plugins {
  `java-library`
  jacoco
  `jvm-test-suite`
  `maven-publish`
  alias(libs.plugins.kotlin.jvm)
}

java {
  withJavadocJar()
  withSourcesJar()
  toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

kotlin { jvmToolchain(21) }

repositories { mavenCentral() }

dependencies {
  api(project(":modules:core"))
  api(libs.jspecify)

  compileOnly(platform(libs.kotest.bom))
  compileOnly(libs.kotest.framework.engine)
}

testing {
  suites {
    val test by
      getting(JvmTestSuite::class) {
        dependencies {
          implementation(platform(libs.kotest.bom))
          implementation(libs.kotest.runner.junit5)
          implementation(libs.assertj.core)
        }
        targets.all { testTask.configure { useJUnitPlatform() } }
      }
  }
}

tasks.jacocoTestReport { reports { xml.required = true } }
