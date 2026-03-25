plugins {
  java
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.spring.dependency.management)
}

group = "org.approvej.examples"

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

repositories { mavenCentral() }

dependencies {
  implementation(libs.spring.boot.starter.data.jpa)
  implementation(libs.spring.boot.starter.thymeleaf)
  implementation(libs.spring.boot.starter.webmvc)
  runtimeOnly(libs.postgresql)

  testImplementation(libs.spring.boot.starter.data.jpa.test)
  testImplementation(libs.spring.boot.starter.webmvc.test)
  testImplementation(libs.spring.boot.testcontainers)
  testImplementation(libs.testcontainers.junit.jupiter)
  testImplementation(libs.testcontainers.postgresql)

  testImplementation(project(":modules:core"))
  testImplementation(project(":modules:image"))
  testImplementation(project(":modules:json-jackson3"))
  testImplementation(project(":modules:yaml-jackson3"))
  testImplementation(project(":modules:http"))
  testImplementation(project(":modules:database-jdbc"))
  testImplementation(platform(libs.jackson3.bom))
  testImplementation(libs.jackson3.dataformat.yaml)
  testImplementation(libs.playwright)
}

tasks.withType<Test> {
  useJUnitPlatform()

  // Skip Spring Boot example tests on Windows because they require Docker/Testcontainers
  if (System.getProperty("os.name").lowercase().contains("windows")) {
    enabled = false
  }
}
