plugins {
  `java-library`
  jacoco
  `jvm-test-suite`
  `maven-publish`
  alias(libs.plugins.maven.plugin.development)
}

java {
  withJavadocJar()
  withSourcesJar()
  toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

repositories { mavenCentral() }

dependencies {
  compileOnly(libs.maven.plugin.annotations)
  implementation(libs.maven.plugin.api)
  implementation(libs.maven.core)
}

tasks.jacocoTestReport { reports { xml.required = true } }

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
