plugins {
  `java-platform`
  `maven-publish`
}

repositories { mavenCentral() }

dependencies {
  constraints {
    rootProject.subprojects
      .filter { it != project && it.name != "manual" && it.subprojects.isEmpty() }
      .sortedBy { it.name }
      .forEach { api(it) }

    api(libs.jackson.databind)
    api(libs.jackson.jsr310)
    api(libs.jackson.dataformat.yaml)
  }
}
