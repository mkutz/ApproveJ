plugins {
  `java-gradle-plugin`
  `jvm-test-suite`
  `maven-publish`
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

gradlePlugin {
  plugins {
    create("approvej") {
      id = "org.approvej"
      implementationClass = "org.approvej.gradle.ApproveJPlugin"
    }
  }
}
