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

    api(libs.jackson2.databind)
    api(libs.jackson2.jsr310)
    api(libs.jackson2.dataformat.yaml)

    api(libs.jackson3.databind)
    api(libs.jackson3.dataformat.yaml)
  }
}
