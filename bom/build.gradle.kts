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
  }
}
