plugins {
  `java-platform`
  `maven-publish`
}

javaPlatform { allowDependencies() }

repositories { mavenCentral() }

dependencies {
  api(platform(libs.jackson2.bom))
  api(platform(libs.jackson3.bom))

  constraints {
    rootProject.subprojects
      .filter {
        it != project &&
          it.name !in listOf("approvej-gradle-plugin", "approvej-maven-plugin", "manual") &&
          it.subprojects.isEmpty()
      }
      .sortedBy { it.name }
      .forEach { api(it) }
  }
}
