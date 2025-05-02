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
  toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

repositories { mavenCentral() }

dependencies { api(libs.jspecify) }

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
  dependsOn(
    testing.suites.named("test"),
    testing.suites.named("testng"),
    testing.suites.named("spock"),
  )
  executionData(fileTree(project.layout.buildDirectory) { include("**/jacoco/*.exec") })
  reports { xml.required = true }
  sourceDirectories.from(sourceSets.main.get().allJava.srcDirs)
  classDirectories.from(sourceSets.main.get().output.classesDirs)
}
