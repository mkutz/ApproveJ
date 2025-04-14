@file:Suppress("UnstableApiUsage", "unused")

plugins {
  java
  `java-test-fixtures`
  `jvm-test-suite`
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.asciidoctor)
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }

repositories { mavenCentral() }

val asciidoctorExt: Configuration by configurations.creating

dependencies { asciidoctorExt(libs.asciidoctorBlockSwitch) }

testing {
  suites {
    val test by
      getting(JvmTestSuite::class) {
        useJUnitJupiter()
        dependencies {
          implementation(project(":modules:core"))
          implementation(project(":modules:json-jackson"))

          implementation(platform(libs.junitBom))
          implementation(libs.junitJupiterApi)
          implementation(libs.junitJupiterParams)
          implementation(libs.assertjCore)

          runtimeOnly(libs.junitPlatformLauncher)
          runtimeOnly(libs.junitJupiterEngine)
        }
      }
  }
}

tasks.withType<org.asciidoctor.gradle.jvm.AsciidoctorTask> {
  baseDirFollowsSourceFile()
  configurations("asciidoctorExt")
}
