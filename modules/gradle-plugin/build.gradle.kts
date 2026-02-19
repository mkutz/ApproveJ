plugins {
  `java-gradle-plugin`
  jacoco
  `jvm-test-suite`
  alias(libs.plugins.plugin.publish)
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

repositories { mavenCentral() }

testing {
  suites {
    val test by
      getting(JvmTestSuite::class) {
        useJUnitJupiter()
        dependencies {
          implementation(platform(libs.junit.bom))
          implementation(libs.junit.jupiter.api)
          implementation(libs.assertj.core)

          runtimeOnly(libs.junit.platform.launcher)
          runtimeOnly(libs.junit.jupiter.engine)
        }
      }
  }
}

tasks.jacocoTestReport { reports { xml.required = true } }

gradlePlugin {
  website = "https://approvej.org"
  vcsUrl = "https://github.com/mkutz/approvej"
  plugins {
    create("approvej") {
      id = "org.approvej"
      implementationClass = "org.approvej.gradle.ApproveJPlugin"
      displayName = "ApproveJ"
      description = "Find and remove orphaned approved files"
      tags = listOf("testing", "approval-testing", "snapshot-testing")
    }
  }
}
