@file:Suppress("UnstableApiUsage", "unused")

plugins {
  java
  `java-test-fixtures`
  `jvm-test-suite`
  alias(libs.plugins.asciidoctor)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.spring)
  alias(libs.plugins.kotlin.jpa)
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.spring.dependency.management)
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }

repositories { mavenCentral() }

val asciidoctorExt: Configuration by configurations.creating

ext["junit-jupiter.version"] = libs.versions.junit.get()

ext["testcontainers.version"] = libs.versions.testcontainers.get()

dependencies {
  asciidoctorExt(libs.asciidoctor.block.switch)

  implementation(libs.kotlin.reflect)
  implementation(libs.spring.boot.starter.data.jpa)
  implementation(libs.spring.boot.starter.web)
  implementation(libs.spring.kafka)
  implementation(libs.jackson.module.kotlin)

  runtimeOnly(libs.postgresql)
}

testing {
  suites {
    val test by
      getting(JvmTestSuite::class) {
        useJUnitJupiter()
        dependencies {
          implementation(project(":modules:core"))
          implementation(project(":modules:json-jackson"))
          implementation(project(":modules:yaml-jackson"))

          implementation(platform(libs.junit.bom))
          implementation(libs.junit.jupiter.api)
          implementation(libs.junit.jupiter.params)
          implementation(libs.assertj.core)
          implementation(libs.kotlin.test.junit5)

          implementation(libs.spring.boot.starter.test)
          implementation(libs.spring.boot.testcontainers)
          implementation(libs.spring.kafka.test)
          implementation(libs.testcontainers.junit.jupiter)
          implementation(libs.testcontainers.kafka)
          implementation(libs.testcontainers.postgresql)
          implementation(libs.datasource.proxy)

          runtimeOnly(libs.junit.platform.launcher)
          runtimeOnly(libs.junit.jupiter.engine)
        }
      }
  }
}

tasks.withType<org.asciidoctor.gradle.jvm.AsciidoctorTask> {
  baseDirFollowsSourceFile()
  configurations("asciidoctorExt")
}

asciidoctorj { modules { diagram.use() } }
