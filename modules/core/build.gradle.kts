@file:Suppress("UnstableApiUsage", "unused")

plugins {
  groovy
  `java-library`
  `java-test-fixtures`
  jacoco
  `jacoco-report-aggregation`
  `jvm-test-suite`
  `maven-publish`
}

java {
  withJavadocJar()
  withSourcesJar()
  toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

repositories { mavenCentral() }

dependencies { api(libs.jspecify) }

testing {
  suites {
    val test by
      getting(JvmTestSuite::class) {
        useJUnitJupiter()
        dependencies {
          implementation(platform(libs.junit.bom))
          implementation(libs.junit.jupiter.api)
          implementation(libs.junit.jupiter.params)
          implementation(libs.assertj.core)
          implementation(libs.awaitility)

          runtimeOnly(libs.junit.platform.launcher)
          runtimeOnly(libs.junit.jupiter.engine)
        }
      }
    val testng by
      registering(JvmTestSuite::class) {
        useTestNG()
        dependencies {
          implementation(libs.testng)
          implementation(project())
        }
      }
    val spock by
      registering(JvmTestSuite::class) {
        useSpock()
        dependencies {
          implementation(libs.spock)
          implementation(libs.groovy)
          implementation(project())
        }
      }
  }
}

tasks.named("check") { dependsOn(testing.suites.named("testng"), testing.suites.named("spock")) }

tasks.jacocoTestReport {
  mustRunAfter(tasks.check, tasks.javadoc)
  executionData(fileTree(project.layout.buildDirectory) { include("**/jacoco/*.exec") })
  reports { xml.required = true }
}
