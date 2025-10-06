@file:Suppress("UnstableApiUsage", "unused")

plugins {
  groovy
  `java-library`
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
  api(libs.diffUtils)
}

testing {
  suites {
    val test by
      getting(JvmTestSuite::class) {
        useJUnitJupiter()
        dependencies {
          implementation(platform(libs.junitBom))
          implementation(libs.junitJupiterApi)
          implementation(libs.junitJupiterParams)
          implementation(libs.assertjCore)
          implementation(libs.awaitility)

          runtimeOnly(libs.junitPlatformLauncher)
          runtimeOnly(libs.junitJupiterEngine)
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
