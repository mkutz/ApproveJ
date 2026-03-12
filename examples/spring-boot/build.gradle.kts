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
  implementation(libs.spring.boot.starter.webmvc)
  runtimeOnly(libs.postgresql)

  testImplementation(libs.spring.boot.starter.data.jpa.test)
  testImplementation(libs.spring.boot.starter.webmvc.test)
  testImplementation(libs.spring.boot.testcontainers)
  testImplementation(libs.testcontainers.junit.jupiter)
  testImplementation(libs.testcontainers.postgresql)

  testImplementation(project(":modules:core"))
  testImplementation(project(":modules:json-jackson3"))
  testImplementation(project(":modules:yaml-jackson3"))
  testImplementation(project(":modules:http"))
  testImplementation(project(":modules:database"))
  testImplementation(libs.jackson3.dataformat.yaml)
}

tasks.withType<Test> {
  useJUnitPlatform()

  // Rancher Desktop runs Docker inside a Lima VM.
  // Ryuk needs the VM-internal socket path, not the host-side path.
  environment("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", "/var/run/docker.sock")
  // Mapped ports are reachable via the VM IP, not localhost.
  val hostIp =
    providers
      .exec {
        commandLine(
          "sh",
          "-c",
          "rdctl shell ip a show rd0 2>/dev/null | awk '/inet / {sub(\"/.*\",\"\"); print \$2}'",
        )
      }
      .standardOutput
      .asText
      .map { it.trim() }
      .orElse("localhost")
  environment("TESTCONTAINERS_HOST_OVERRIDE", hostIp)
}
