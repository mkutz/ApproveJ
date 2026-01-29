@file:Suppress("UnstableApiUsage", "unused")

plugins {
  java
  `java-test-fixtures`
  `jvm-test-suite`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.asciidoctor)
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }

repositories { mavenCentral() }

val asciidoctorExt: Configuration by configurations.creating

dependencies { asciidoctorExt(libs.asciidoctor.block.switch) }

testing {
  suites {
    val test by
      getting(JvmTestSuite::class) {
        useJUnitJupiter()
        dependencies {
          implementation(project(":modules:core"))
          implementation(project(":modules:json-jackson"))
          implementation(project(":modules:yaml-jackson"))

          implementation(libs.jackson2.databind)
          implementation(libs.jackson2.dataformat.yaml)
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

tasks.withType<org.asciidoctor.gradle.jvm.AsciidoctorTask> {
  baseDirFollowsSourceFile()
  configurations("asciidoctorExt")
}

asciidoctorj { modules { diagram.use() } }
