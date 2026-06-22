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

dependencies {
  api(libs.jspecify)
  compileOnlyApi(platform(libs.junit.bom))
  compileOnlyApi(libs.junit.jupiter.api)
}

testing {
  suites {
    getByName<JvmTestSuite>("test") {
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
    register<JvmTestSuite>("testng") {
      useTestNG()
      dependencies {
        implementation(libs.testng)
        implementation(project())
      }
    }
    register<JvmTestSuite>("spock") {
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
