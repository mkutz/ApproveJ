@file:Suppress("UnstableApiUsage", "unused")

plugins {
  `java-library`
  `maven-publish`
  alias(libs.plugins.jreleaser)
}

java {
  withJavadocJar()
  withSourcesJar()
  toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

repositories { mavenCentral() }

dependencies {
  api(libs.jspecify)
  api(libs.jacksonDatabind)
  api(libs.jsonpath)
  api(project(":modules:core"))
}

testing {
  suites {
    val test by
      getting(JvmTestSuite::class) {
        useJUnitJupiter()
        dependencies {
          implementation(libs.jacksonJsr310)
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
